package com.sonetica.topface.ui.likes;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.module.pull2refresh.PullToRefreshGridView;
import com.sonetica.topface.module.pull2refresh.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.LikesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.DoubleButton;
import com.sonetica.topface.ui.GalleryManager;
import com.sonetica.topface.ui.album.AlbumActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

/*
 *      "я нравлюсь"
 */
public class LikesActivity extends Activity {
  // Data
  private PullToRefreshGridView mGallery;
  private LikesGridAdapter mLikesGridAdapter;
  private GalleryManager mGalleryManager;
  private LinkedList<Like> mLikesAllList;
  private LinkedList<Like> mLikesCityList;
  private ProgressDialog mProgressDialog;
  private int mCity;                                 // ГОРОД БРАТЬ ИЗ ПРОФАЙЛА
  private int mCurrentCity; 
  // Constats
  private static final int ALL_CITIES = 0;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likes);
    Debug.log(this,"+onCreate");
    
    
    SharedPreferences preferences = getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    mCity = preferences.getInt(getString(R.string.s_likes_city_id),0);
    mCurrentCity = mCity = 2;
    
    // Data
    mLikesAllList  = Data.s_LikesList;
    mLikesCityList = new LinkedList<Like>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.likes_header_title));
   
   // Double Button
   DoubleButton btnDouble = (DoubleButton)findViewById(R.id.btnDoubleLikes);
   btnDouble.setLeftText(getString(R.string.likes_btn_dbl_left));
   btnDouble.setRightText(getString(R.string.likes_btn_dbl_right));
   btnDouble.setChecked(mCity==0?DoubleButton.LEFT_BUTTON:DoubleButton.RIGHT_BUTTON);
   btnDouble.setLeftListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       mCurrentCity = ALL_CITIES;
       update();
     }
   });
   btnDouble.setRightListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       mCurrentCity = mCity;
       update();
     }
   });

   // Gallery
   mGallery = (PullToRefreshGridView)findViewById(R.id.grdLikesGallary);
   mGallery.setAnimationCacheEnabled(false);
   mGallery.setNumColumns(getResources().getInteger(R.integer.grid_column_number));
   //mGallery.setScrollingCacheEnabled(false);
   mGallery.setOnScrollListener(new OnScrollListener() {
     @Override
     public void onScrollStateChanged(AbsListView view,int scrollState) {
       if(scrollState==SCROLL_STATE_IDLE) {
         mGalleryManager.mRunning=true;
         mGallery.invalidateViews();
       } else
         mGalleryManager.mRunning=false;
     }
     @Override
     public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
     }
   });
   mGallery.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(LikesActivity.this,AlbumActivity.class);
       intent.putExtra(AlbumActivity.INTENT_USER_ID,mLikesAllList.get(position).uid);
       startActivityForResult(intent,0);
     }
   });
   mGallery.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       //update();
       mGallery.onRefreshComplete();
     }
   });
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   create();
   
   if(mLikesAllList.size()==0)
     update();
   
   // обнуление информера непросмотренных лайков
   Data.s_Likes = 0;
  }
  //---------------------------------------------------------------------------
  private void create() {
    mGalleryManager   = new GalleryManager(LikesActivity.this,mLikesAllList);
    mLikesGridAdapter = new LikesGridAdapter(LikesActivity.this,mGalleryManager);
    mGallery.getRefreshableView().setAdapter(mLikesGridAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mGallery!=null)          mGallery=null;
    if(mLikesGridAdapter!=null) mLikesGridAdapter=null;
    if(mLikesAllList!=null)     mLikesAllList=null;
    if(mProgressDialog!=null)   mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
  private void update() {
    mProgressDialog.show();
    
    LikesRequest likesRequest = new LikesRequest(this);
    likesRequest.offset = 0;
    likesRequest.limit  = 40;
    likesRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        mLikesAllList.addAll(Like.parse(response));
        
        //int size = mLikesAllList.size();     // обычный фор!!!???
        for(Like like : mLikesAllList)
          if(like.city_id==mCity)
            mLikesCityList.add(like);        // ЧТО ЭТО
        
        if(mCurrentCity==ALL_CITIES) {
          mGalleryManager.setDataList(mLikesAllList);
          mLikesGridAdapter.collapse(false);
        } else {
          //mGalleryManager.setDataList(mLikesAllList);
          mGalleryManager.setDataList(mLikesCityList);
          mLikesGridAdapter.collapse(true);
        }

        mLikesGridAdapter.notifyDataSetChanged();
        
        mGallery.onRefreshComplete();
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    mGalleryManager.restart();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    // Сохранение параметров
    SharedPreferences preferences = getSharedPreferences(Global.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt(getString(R.string.s_likes_city_id),mCity);
    editor.commit();
    
    release();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
