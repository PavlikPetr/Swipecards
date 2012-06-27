package com.topface.topface.ui.frames;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.FeedSympathy;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedSympathyRequest;
import com.topface.topface.ui.adapters.SymphatyListAdapter;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SympathyActivity extends FrameActivity {
  // Data
  private boolean mNewUpdating;
  private TextView mFooterView;
  private PullToRefreshListView mListView;
  private SymphatyListAdapter mListAdapter;
  private AvatarManager<FeedSympathy> mAvatarManager;
  private DoubleBigButton mDoubleButton;
  private ProgressBar mProgressBar;
  private ImageView mBannerView;
  // Constants
  private static final int LIMIT = 44;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_sympathy);
    Debug.log(this,"+onCreate");

    // Data
    Data.sympathyList = new LinkedList<FeedSympathy>();
   
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
    mListView = (PullToRefreshListView)findViewById(R.id.lvSymphatyList);
    mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
          Intent intent = new Intent(SympathyActivity.this.getApplicationContext(), ProfileActivity.class);
          intent.putExtra(ProfileActivity.INTENT_USER_ID,   Data.sympathyList.get(position).uid);
          intent.putExtra(ProfileActivity.INTENT_USER_NAME, Data.sympathyList.get(position).first_name);
          startActivityForResult(intent,0);
        } catch(Exception e) {
          Debug.log(SympathyActivity.this, "start ProfileActivity exception:" + e.toString());
        }
      }
    });
    mListView.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        updateData(true);
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
    mAvatarManager = new AvatarManager<FeedSympathy>(this, Data.sympathyList);
    mListAdapter = new SymphatyListAdapter(getApplicationContext(),mAvatarManager);
    mListView.setOnScrollListener(mAvatarManager);
    mListView.setAdapter(mListAdapter);
    
   
    mNewUpdating = CacheProfile.unread_symphaties > 0 ? true : false;
    CacheProfile.unread_symphaties = 0;
  }
  //---------------------------------------------------------------------------
  private void updateData(boolean isPushUpdating) {
    if(!isPushUpdating)
      mProgressBar.setVisibility(View.VISIBLE);
    
    mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
    
    FeedSympathyRequest symphatyRequest = new FeedSympathyRequest(getApplicationContext());
    symphatyRequest.limit = LIMIT;
    symphatyRequest.only_new = mNewUpdating;
    symphatyRequest.callback(new ApiHandler(){
      @Override
      public void success(ApiResponse response) {
        Data.sympathyList.clear();
        Data.sympathyList.addAll(FeedSympathy.parse(response));
        updateUI(new Runnable() {
          @Override
          public void run() {
            if(mNewUpdating)
              mFooterView.setVisibility(View.GONE);
            else
              mFooterView.setVisibility(View.VISIBLE);
            
            if(Data.sympathyList.size()==0 || Data.sympathyList.size()<LIMIT/2)
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
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(SympathyActivity.this,getString(R.string.general_data_error),Toast.LENGTH_SHORT).show();
            mListView.onRefreshComplete(); 
          }
        });
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void updateDataHistory() {
    mProgressBar.setVisibility(View.VISIBLE);
    
    FeedSympathyRequest symphatyRequest = new FeedSympathyRequest(getApplicationContext());
    symphatyRequest.limit = LIMIT;
    symphatyRequest.only_new = false;
    symphatyRequest.from = Data.sympathyList.get(Data.sympathyList.size()-1).id;
    symphatyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedSympathy> feedSymphatyList = FeedSympathy.parse(response);
        if(feedSymphatyList.size() > 0)
          Data.sympathyList.addAll(feedSymphatyList);
        updateUI(new Runnable() {
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
        updateUI(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(SympathyActivity.this,getString(R.string.general_data_error),Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
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
    Debug.log(this,"SympathyActivity::clearLayout");
    mListView.setVisibility(View.INVISIBLE);
  }
  //---------------------------------------------------------------------------
  @Override
  public void fillLayout() {
    Debug.log(this,"SympathyActivity::fillLayout");
    
    updateBanner(mBannerView, BannerRequest.SYMPATHY);
    updateData(false);
  }
  //---------------------------------------------------------------------------
  @Override
  public void release() {
    mListView=null;
    
    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter = null;
    
    if(mAvatarManager!=null) {
      mAvatarManager.release();
      mAvatarManager=null;
    }
  }
  //---------------------------------------------------------------------------
}