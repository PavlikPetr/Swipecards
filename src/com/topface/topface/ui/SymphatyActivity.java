package com.topface.topface.ui;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Banner;
import com.topface.topface.data.FeedSymphaty;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedSymphatyRequest;
import com.topface.topface.ui.adapters.SymphatyListAdapter;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;

import com.topface.topface.utils.*;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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

public class SymphatyActivity extends Activity {
  // Data
  private boolean mNewUpdating;
  private TextView mFooterView;
  private PullToRefreshListView mListView;
  private SymphatyListAdapter mListAdapter;
  private LinkedList<FeedSymphaty> mSymphatyDataList;
  private AvatarManager<FeedSymphaty> mAvatarManager;
  private DoubleBigButton mDoubleButton;
  private ProgressBar mProgressBar;
  private FeedSymphatyRequest symphatyRequest;
  private ImageView mBannerView;
  // Constants
  private static final int LIMIT = 44;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_symphaty);
    Debug.log(this,"+onCreate");

    // Data
    mSymphatyDataList = new LinkedList<FeedSymphaty>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.symphaty_header_title));
   
   // Progress
   mProgressBar = (ProgressBar)findViewById(R.id.prsSymphatyLoading);
   
   // Banner
   mBannerView = (ImageView)findViewById(R.id.ivBanner);
   
   // Double Button
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.symphaty_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.symphaty_btn_dbl_right));
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
   mListView = (PullToRefreshListView)findViewById(R.id.lvSymphatyList);
   mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(SymphatyActivity.this.getApplicationContext(),ProfileActivity.class);
       intent.putExtra(ProfileActivity.INTENT_USER_ID,mSymphatyDataList.get(position).uid);
       intent.putExtra(ProfileActivity.INTENT_USER_NAME,mSymphatyDataList.get(position).first_name);
       startActivityForResult(intent,0);
     }
   });
   mListView.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(true);
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
   mAvatarManager = new AvatarManager<FeedSymphaty>(mSymphatyDataList);
   mListAdapter = new SymphatyListAdapter(getApplicationContext(),mAvatarManager);
   mListView.setOnScrollListener(mAvatarManager);
   mListView.setAdapter(mListAdapter);
   
   
   mNewUpdating = CacheProfile.unread_symphaties > 0;
   CacheProfile.unread_symphaties = 0;
   
   banner();
   update(false);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    if(symphatyRequest!=null) symphatyRequest.cancel();
    
    release();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void update(boolean isPushUpdating) {
    if(!isPushUpdating)
      mProgressBar.setVisibility(View.VISIBLE);
    mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
    symphatyRequest = new FeedSymphatyRequest(getApplicationContext());
    symphatyRequest.limit = LIMIT;
    symphatyRequest.only_new = mNewUpdating;
    symphatyRequest.callback(new ApiHandler(){
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedSymphaty> feedSymphatyList = FeedSymphaty.parse(response);
        mSymphatyDataList.clear();
        mSymphatyDataList.addAll(feedSymphatyList);
        post(new Runnable() {
          @Override
          public void run() {
            if(mNewUpdating)
              mFooterView.setVisibility(View.GONE);
            else
              mFooterView.setVisibility(View.VISIBLE);
            
            if(feedSymphatyList.size()==0 || feedSymphatyList.size()<LIMIT/2)
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
            mProgressBar.setVisibility(View.GONE);
            Utils.showErrorMessage(SymphatyActivity.this);
            if (mListView != null) {
                mListView.onRefreshComplete();
            }
          }
        });
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void history() {
    mProgressBar.setVisibility(View.VISIBLE);
    symphatyRequest = new FeedSymphatyRequest(getApplicationContext());
    symphatyRequest.limit = LIMIT;
    symphatyRequest.only_new = false;
    symphatyRequest.from = mSymphatyDataList.get(mSymphatyDataList.size()-1).id;
    symphatyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedSymphaty> feedSymphatyList = FeedSymphaty.parse(response);
        if(feedSymphatyList.size() > 0)
          mSymphatyDataList.addAll(feedSymphatyList);
        post(new Runnable() {
          @Override
          public void run() {
            if(feedSymphatyList.size()==0 || feedSymphatyList.size()<LIMIT/2)
              mFooterView.setVisibility(View.GONE);
            else
              mProgressBar.setVisibility(View.VISIBLE);
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
            Utils.showErrorMessage(SymphatyActivity.this);
            mProgressBar.setVisibility(View.GONE);
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
                    intent = new Intent(SymphatyActivity.this, BuyingActivity.class); // "parameter":"PURCHASE"
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
    mListView=null;
    
    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter = null;
    
    if(mAvatarManager!=null) {
      mAvatarManager.release();
      mAvatarManager=null;
    }
    
    if(mSymphatyDataList!=null)
      mSymphatyDataList.clear();
    mSymphatyDataList=null;
  }
  //---------------------------------------------------------------------------
}