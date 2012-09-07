package com.topface.topface.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedLikesRequest;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class LikesActivity extends TrackedActivity {
  // Data
  private boolean mNewUpdating;
  private TextView mFooterView;
  private PullToRefreshListView mListView;
  private LikesListAdapter mListAdapter;
  private LinkedList<FeedLike> mLikesDataList;
  private AvatarManager<FeedLike> mAvatarManager;
  private DoubleBigButton mDoubleButton;
  private ProgressBar mProgressBar;
  private FeedLikesRequest likesRequest;
  // Constants
  private static final int LIMIT = 44;
    private FloatBlock mFloatBlock;

    //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likes);
    Debug.log(this,"+onCreate");
    
    // Data
    mLikesDataList = new LinkedList<FeedLike>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.likes_header_title));
   
   // Progress
   mProgressBar = (ProgressBar)findViewById(R.id.prsLikesLoading);

   // Double Button
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.likes_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.likes_btn_dbl_right));
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
   mListView = (PullToRefreshListView)findViewById(R.id.lvLikesList);
   mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener(){
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
       intent.putExtra(ProfileActivity.INTENT_USER_ID,mLikesDataList.get(position).uid);
       intent.putExtra(ProfileActivity.INTENT_USER_NAME,mLikesDataList.get(position).first_name);
       intent.putExtra(ProfileActivity.INTENT_MUTUAL_ID,mLikesDataList.get(position).id);
       startActivity(intent);
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
   mAvatarManager = new AvatarManager<FeedLike>(mLikesDataList);
   mListAdapter = new LikesListAdapter(getApplicationContext(),mAvatarManager);
   mListView.setAdapter(mListAdapter);
   
   mNewUpdating = CacheProfile.unread_likes > 0;
   CacheProfile.unread_likes = 0;
   
   update(false);
   mFloatBlock = new FloatBlock(this);

  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    if(likesRequest!=null) likesRequest.cancel();
    
    release();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }

    @Override
    protected void onResume() {
        super.onResume();
        mFloatBlock.update();
    }

    //---------------------------------------------------------------------------
  private void update(boolean isPushUpdating) {
    if(!isPushUpdating)
      mProgressBar.setVisibility(View.VISIBLE);
    mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
    likesRequest = new FeedLikesRequest(getApplicationContext());
    likesRequest.limit = LIMIT;
    likesRequest.only_new = mNewUpdating;
    likesRequest.callback(new ApiHandler(){
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedLike> feedLikesList = FeedLike.parse(response);
        mLikesDataList.clear();
        mLikesDataList.addAll(feedLikesList);
        post(new Runnable() {
          @Override
          public void run() {
            if(mNewUpdating)
              mFooterView.setVisibility(View.GONE);
            else
              mFooterView.setVisibility(View.VISIBLE);
            
            if(feedLikesList.size()==0 || feedLikesList.size()<LIMIT/2)
              mFooterView.setVisibility(View.GONE);
            
            mProgressBar.setVisibility(View.GONE);
            if (mListView != null) {
                mListView.onRefreshComplete();
            }
            if (mListAdapter != null) {
                mListAdapter.notifyDataSetChanged();
            }
          }
        });
      }
      @Override
      public void fail(int codeError, ApiResponse response) {
        post(new Runnable() {
          @Override
          public void run() {
            Utils.showErrorMessage(LikesActivity.this);
            mProgressBar.setVisibility(View.GONE);
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
    likesRequest = new FeedLikesRequest(getApplicationContext());
    likesRequest.limit = LIMIT;
    likesRequest.only_new = false;
    likesRequest.from = mLikesDataList.get(mLikesDataList.size()-1).id;
    likesRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final LinkedList<FeedLike> feedLikesList = FeedLike.parse(response);
        if(feedLikesList.size() > 0)
          mLikesDataList.addAll(feedLikesList);
        post(new Runnable() {
          @Override
          public void run() {
            if(feedLikesList.size()==0 || feedLikesList.size()<LIMIT/2)
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
            if (mFooterView != null && mProgressBar != null) {
                mFooterView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
            }
            Utils.showErrorMessage(LikesActivity.this);
            if (mListView != null) {
                mListView.onRefreshComplete();
            }
          }
        });
      }
    }).exec();
  }

  private void release() {
    mListView=null;
    
    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter = null;

    mAvatarManager=null;
    
    if(mLikesDataList!=null)
      mLikesDataList.clear();
    mLikesDataList = null;
  }
  //---------------------------------------------------------------------------
}
