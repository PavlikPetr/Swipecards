package com.topface.topface.ui.frames;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.data.FeedInbox;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedInboxRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.adapters.InboxListAdapter;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class InboxActivity extends FrameActivity {
  // Data
  private boolean mNewUpdating;
  private TextView mFooterView;
  private PullToRefreshListView mListView;
  private InboxListAdapter mListAdapter;
  private AvatarManager<FeedInbox> mAvatarManager;
  private DoubleBigButton mDoubleButton;
  private ProgressBar mProgressBar;
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
    Data.inboxList = new LinkedList<FeedInbox>();

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
        updateData(false);
      }
    });
    mDoubleButton.setRightListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mNewUpdating = true;
        updateData(false);
      }
    });
   
    // ListView
    mListView = (PullToRefreshListView)findViewById(R.id.lvInboxList);
    mListView.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        updateData(true);
      }
    });
    mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ImageView iv = (ImageView)view.findViewById(R.id.ivAvatar);
        Data.userAvatar = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        try {
          Intent intent = new Intent(InboxActivity.this.getApplicationContext(), ChatActivity.class);
          intent.putExtra(ChatActivity.INTENT_USER_ID,   Data.inboxList.get(position).uid);
          intent.putExtra(ChatActivity.INTENT_USER_NAME, Data.inboxList.get(position).first_name);
          startActivity(intent);
        } catch(Exception e) {
          Debug.log(InboxActivity.this, "start ChatActivity exception:" + e.toString());
        }
      }
    });
    
    // Footer
    mFooterView = new TextView(getApplicationContext());
    mFooterView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        updateDataHistory();
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
    mAvatarManager = new AvatarManager<FeedInbox>(getApplicationContext(), Data.inboxList);
    mListAdapter = new InboxListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setOnScrollListener(mAvatarManager);    
    mListView.setAdapter(mListAdapter);
    
    mNewUpdating = CacheProfile.unread_messages > 0 ? true : false;
    CacheProfile.unread_messages = 0;
  }
  //---------------------------------------------------------------------------
  private void updateData(boolean isPushUpdating) {
    if(!isPushUpdating)
      mProgressBar.setVisibility(View.VISIBLE);
    
    mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
    
    FeedInboxRequest inboxRequest = new FeedInboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = mNewUpdating;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Data.inboxList.clear();
        Data.inboxList.addAll(FeedInbox.parse(response));
        updateUI(new Runnable() {
          @Override
          public void run() {
            if(mNewUpdating)
              mFooterView.setVisibility(View.GONE);
            else
              mFooterView.setVisibility(View.VISIBLE);
            
            if(Data.inboxList.size()==0 || Data.inboxList.size()<LIMIT/2)
              mFooterView.setVisibility(View.GONE);
            
            mProgressBar.setVisibility(View.GONE);
            mListView.onRefreshComplete(); 
            mListAdapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        updateUI(new Runnable() {
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
  private void updateDataHistory() {
    mProgressBar.setVisibility(View.VISIBLE);
    
    FeedInboxRequest inboxRequest = new FeedInboxRequest(getApplicationContext());
    inboxRequest.limit = LIMIT;
    inboxRequest.only_new = false;
    inboxRequest.from = Data.inboxList.get(Data.inboxList.size()-1).id;
    inboxRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedInbox> feedInboxList = FeedInbox.parse(response);
        if(feedInboxList.size() > 0)
          Data.inboxList.addAll(feedInboxList);
        
        updateUI(new Runnable() {
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
        updateUI(new Runnable() {
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
  // FrameActivity
  //---------------------------------------------------------------------------
  @Override
  public void clearLayout() {
    Debug.log(this,"InboxActivity::clearLayout");
    mListView.setVisibility(View.INVISIBLE);
  }
  //---------------------------------------------------------------------------
  @Override
  public void fillLayout() {
    Debug.log(this,"InboxActivity::fillLayout");
    
    updateBanner(mBannerView, BannerRequest.INBOX);
    updateData(false);
  }
  //---------------------------------------------------------------------------
  @Override
  public void release() {
    mListView = null;
    
    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter = null;
    
    if(mAvatarManager!=null)
      mAvatarManager.release();
    mAvatarManager = null;

    Data.userAvatar = null;
  }
  //---------------------------------------------------------------------------
}