package com.topface.topface.ui.likes;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.p2r.PullToRefreshGridView;
import com.topface.topface.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedLikesRequest;
import com.topface.topface.ui.DoubleBigButton;
import com.topface.topface.ui.GalleryGridManager;
import com.topface.topface.ui.ThumbView;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/*
 *      "я нравлюсь"
 */
public class LikesActivity extends Activity {
  // Data
  private boolean mOnlyNewData;
  private PullToRefreshGridView mGallery;
  private LikesGridAdapter mAdapter;
  private GalleryGridManager<FeedLike> mGalleryGridManager;
  private LinkedList<FeedLike> mLikesDataList;
  private ProgressDialog mProgressDialog;
  private DoubleBigButton mDoubleButton;
  // Constants
  private static final int LIMIT = 84;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likes);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Data
    mLikesDataList  = new LinkedList<FeedLike>();
   
   // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.likes_header_title));

   // Double Button
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.likes_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.likes_btn_dbl_right));
   mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
   mDoubleButton.setLeftListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       mOnlyNewData = false;
       update(true);
     }
   });
   mDoubleButton.setRightListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       mOnlyNewData = true;
       update(true);
     }
   });

   // Gallery
   mGallery = (PullToRefreshGridView)findViewById(R.id.grdLikesGallary);
   mGallery.setAnimationCacheEnabled(false);
   mGallery.setNumColumns(Data.s_gridColumn);
   mGallery.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(LikesActivity.this.getApplicationContext(),ProfileActivity.class);
       intent.putExtra(ProfileActivity.INTENT_USER_ID,mLikesDataList.get(position).uid);
       intent.putExtra(ProfileActivity.INTENT_USER_NAME,mLikesDataList.get(position).first_name);
       startActivityForResult(intent,0);
     }
   });
   mGallery.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(false);
     }
   });
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this); // getApplicationContext() падает
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   mOnlyNewData = Data.s_Likes > 0 ? true : false;
   
   create();
   update(true);

   // обнуление информера непросмотренных лайков
   Data.s_Likes = 0;
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    //App.bind(getBaseContext());
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
    //App.unbind();
    super.onStop();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    release();
    ThumbView.release();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void create() {
    mGalleryGridManager = new GalleryGridManager<FeedLike>(getApplicationContext(),mLikesDataList);
    mAdapter = new LikesGridAdapter(getApplicationContext(),mGalleryGridManager);
    mGallery.getRefreshableView().setAdapter(mAdapter);
    mGallery.setOnScrollListener(mGalleryGridManager);
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress) {
    if(isProgress)
      mProgressDialog.show();
    FeedLikesRequest likesRequest = new FeedLikesRequest(getApplicationContext());
    likesRequest.limit = LIMIT;
    likesRequest.only_new = mOnlyNewData;
    likesRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        mDoubleButton.setChecked(mOnlyNewData ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
        mLikesDataList.clear();
        mLikesDataList = FeedLike.parse(response);
        mGalleryGridManager.setDataList(mLikesDataList);
        mAdapter.notifyDataSetChanged();
        mProgressDialog.cancel();
        mGallery.onRefreshComplete();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        mProgressDialog.cancel();
        mGallery.onRefreshComplete();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mGalleryGridManager!=null) { 
      mGalleryGridManager.release();
      mGalleryGridManager=null;
    }

    mGallery=null;
    
    if(mAdapter!=null)
      mAdapter.release();
    mAdapter = null;

    mLikesDataList=null;
    mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
}
