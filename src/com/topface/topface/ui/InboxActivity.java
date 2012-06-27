package com.topface.topface.ui;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Banner;
import com.topface.topface.data.FeedInbox;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedInboxRequest;
import com.topface.topface.ui.adapters.InboxListAdapter;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.Http;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class InboxActivity extends Activity {
  // Data
  private boolean mNewUpdating;
  private TextView mFooterView;
  private PullToRefreshListView mListView;
  private InboxListAdapter mListAdapter;
  private LinkedList<FeedInbox> mInboxDataList;
  private AvatarManager<FeedInbox> mAvatarManager;
  private DoubleBigButton mDoubleButton;
  private ProgressBar mProgressBar;
  private FeedInboxRequest inboxRequest;
  private ImageView mBannerView;
  // Constants
  private static final int LIMIT = 40;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_inbox);
    Debug.log(this,"+onCreate");
    
    // Data
    mInboxDataList = new LinkedList<FeedInbox>();
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.inbox_header_title));
    
    // Progress
    mProgressBar = (ProgressBar)findViewById(R.id.prsInboxLoading);
    
    // Banner
    mBannerView = (ImageView)findViewById(R.id.ivBanner);
    
    // Double Button
    mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
    mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
    mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
    mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
    mDoubleButton.setLeftListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mNewUpdating = false;
        update(false);
      }
    });
    mDoubleButton.setRightListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mNewUpdating = true;
        update(false);
      }
    });
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvInboxList);
    mListView.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        update(true);
      }
    });
    mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageView iv = (ImageView)view.findViewById(R.id.ivAvatar);
        Data.userAvatar = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        
        Intent intent = new Intent(InboxActivity.this.getApplicationContext(),ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mInboxDataList.get(position).uid);
        intent.putExtra(ChatActivity.INTENT_USER_NAME,mInboxDataList.get(position).first_name);
        startActivity(intent);
      }
    });
    
    // Footer
    mFooterView = new TextView(getApplicationContext());
    mFooterView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        history();
      }
    });
    mFooterView.setBackgroundResource(R.drawable.item_all_selector);
    mFooterView.setText(getString(R.string.general_footer_previous));
    mFooterView.setTextColor(Color.DKGRAY);
    mFooterView.setGravity(Gravity.CENTER);
    mFooterView.setTypeface(Typeface.DEFAULT_BOLD);
    mFooterView.setVisibility(View.GONE);
    mListView.getRefreshableView().addFooterView(mFooterView);

    // Control creating
    mAvatarManager = new AvatarManager<FeedInbox>(getApplicationContext(),mInboxDataList);
    mListAdapter = new InboxListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setOnScrollListener(mAvatarManager);    
    mListView.setAdapter(mListAdapter);
    
    mNewUpdating = CacheProfile.unread_messages > 0 ? true : false;
    CacheProfile.unread_messages = 0;
    
    banner();
    update(false);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    if(inboxRequest!=null) inboxRequest.cancel();
    
    release();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void update(boolean isPushUpdating) {
    if(!isPushUpdating)
      mProgressBar.setVisibility(View.VISIBLE);
    mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
    inboxRequest = new FeedInboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = mNewUpdating;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedInbox> feedInboxList = FeedInbox.parse(response);
        mInboxDataList.clear();
        mInboxDataList.addAll(feedInboxList);
        post(new Runnable() {
          @Override
          public void run() {
            if(mNewUpdating)
              mFooterView.setVisibility(View.GONE);
            else
              mFooterView.setVisibility(View.VISIBLE);
            
            if(feedInboxList.size()==0 || feedInboxList.size()<LIMIT/2)
              mFooterView.setVisibility(View.GONE);
            
            mProgressBar.setVisibility(View.GONE);
            mListView.onRefreshComplete(); 
            mListAdapter.notifyDataSetChanged();
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        post(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(InboxActivity.this,getString(R.string.general_data_error),Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            mListView.onRefreshComplete();
          }
        });
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void history() {
    mProgressBar.setVisibility(View.VISIBLE);
    inboxRequest = new FeedInboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = false;
    inboxRequest.from = mInboxDataList.get(mInboxDataList.size()-1).id;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedInbox> feedInboxList = FeedInbox.parse(response);
        if(feedInboxList.size() > 0)
          mInboxDataList.addAll(feedInboxList);
        post(new Runnable() {
          @Override
          public void run() {
            if(feedInboxList.size()==0 || feedInboxList.size()<LIMIT/2)
              mFooterView.setVisibility(View.GONE);
            else
              mFooterView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mListView.onRefreshComplete();
            mListAdapter.notifyDataSetChanged();
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        post(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(InboxActivity.this,getString(R.string.general_data_error),Toast.LENGTH_SHORT).show();
            mFooterView.setVisibility(View.GONE);
            mListView.onRefreshComplete();
          }
        });
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void banner() {
    if(Data.screen_width<=Device.W_240)
      return;
    BannerRequest bannerRequest = new BannerRequest(getApplicationContext());
    bannerRequest.place = BannerRequest.LIKE;
    bannerRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final Banner banner = Banner.parse(response);
        if(mBannerView != null)
          post(new Runnable() {
            @Override
            public void run() {
              Http.bannerLoader(banner.url,mBannerView);
              mBannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  Intent intent = null;
                  if(banner.action.equals(Banner.ACTION_PAGE))
                    intent = new Intent(InboxActivity.this, BuyingActivity.class); // "parameter":"PURCHASE"
                  else if(banner.action.equals(Banner.ACTION_URL)) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                  }
                  startActivity(intent);
                }
              });
            }
          });// post
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void release() {
    mListView = null;
    
    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter = null;
    
    if(mAvatarManager!=null)
      mAvatarManager.release();
    mAvatarManager = null;
    
    if(mInboxDataList!=null)
      mInboxDataList.clear();
    mInboxDataList = null;

    Data.userAvatar = null;
  }
  //---------------------------------------------------------------------------
}