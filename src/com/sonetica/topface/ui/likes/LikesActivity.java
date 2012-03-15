package com.sonetica.topface.ui.likes;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.module.pull2refresh.PullToRefreshGridView;
import com.sonetica.topface.module.pull2refresh.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.LikesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.DoubleBigButton;
import com.sonetica.topface.ui.GalleryGridManager;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
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
  private GalleryGridManager<Like> mGalleryGridManager;
  private LinkedList<Like> mLikesDataList;
  private ProgressDialog mProgressDialog;
  private DoubleBigButton mDoubleButton;
  // Constants
  private static final int LIMIT = 60;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likes);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Data
    mLikesDataList  = new LinkedList<Like>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.likes_header_title));
   
   // Double Button
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
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
  private void update(boolean isProgress) {
    if(isProgress)
      mProgressDialog.show();
    LikesRequest likesRequest = new LikesRequest(getApplicationContext());
    likesRequest.limit = LIMIT;
    likesRequest.only_new = mOnlyNewData;
    likesRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        mDoubleButton.setChecked(mOnlyNewData ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);
        mLikesDataList.clear();
        mLikesDataList = Like.parse(response);
        mGalleryGridManager.setDataList(mLikesDataList);
        mAdapter.notifyDataSetChanged();
        mProgressDialog.cancel();
        mGallery.onRefreshComplete();
      }
      @Override
      public void fail(int codeError,Response response) {
        mProgressDialog.cancel();
        //update(true);
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void create() {
    mGalleryGridManager = new GalleryGridManager<Like>(getApplicationContext(),mLikesDataList);
    mAdapter = new LikesGridAdapter(getApplicationContext(),mGalleryGridManager);
    mGallery.getRefreshableView().setAdapter(mAdapter);
    mGallery.setOnScrollListener(mGalleryGridManager);
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
  @Override
  protected void onDestroy() {
    // Сохранение параметров
    //SharedPreferences preferences = getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    //SharedPreferences.Editor editor = preferences.edit();
    //editor.putInt(getString(R.string.s_likes_city_id),mCurrentCity);
    //editor.commit();
    
    release();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
