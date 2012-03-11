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
import com.sonetica.topface.ui.GalleryManager;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.utils.Debug;
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
  private PullToRefreshGridView mGallery;
  private LikesGridAdapter mLikesGridAdapter;
  private GalleryManager<Like> mGalleryManager;
  private LinkedList<Like> mLikesDataList;
  private ProgressDialog mProgressDialog;
  private DoubleBigButton mDoubleButton;
  // Constants
  private static final int LIMIT = 42;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likes);
    Debug.log(this,"+onCreate");
    
    // Data
    mLikesDataList  = new LinkedList<Like>();
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.likes_header_title));
   
   // Double Button
   //DoubleButton btnDouble = (DoubleButton)findViewById(R.id.btnDouble);
   mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
   mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
   mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
   mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
   mDoubleButton.setLeftListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       update(true,false);
     }
   });
   mDoubleButton.setRightListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       update(true,true);
     }
   });

   // Gallery
   mGallery = (PullToRefreshGridView)findViewById(R.id.grdLikesGallary);
   mGallery.setAnimationCacheEnabled(false);
   mGallery.setNumColumns(getResources().getInteger(R.integer.grid_column_number));
   mGallery.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(LikesActivity.this,ProfileActivity.class);
       intent.putExtra(ProfileActivity.INTENT_USER_ID,mLikesDataList.get(position).uid);
       startActivityForResult(intent,0);
     }
   });
   mGallery.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update(false,true);
       mGallery.onRefreshComplete();
     }
   });
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   create();
   
   update(true,Data.s_Likes>0?true:false);
   
   // обнуление информера непросмотренных лайков
   Data.s_Likes = 0;
  }
  //---------------------------------------------------------------------------
  private void update(boolean isProgress, final boolean isNew) {
    if(isProgress)
      mProgressDialog.show();
    LikesRequest likesRequest = new LikesRequest(this);
    likesRequest.limit = LIMIT;
    likesRequest.only_new = isNew;
    likesRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<Like> likesList = Like.parse(response);
        if(likesList.size()>0) {
          mLikesDataList.clear();
          mLikesDataList=likesList;
          mDoubleButton.setChecked(isNew==false?DoubleBigButton.LEFT_BUTTON:DoubleBigButton.RIGHT_BUTTON);        
          mGalleryManager.setDataList(mLikesDataList);
          mLikesGridAdapter.notifyDataSetChanged();
        } else
          mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mProgressDialog.cancel();
        mGallery.onRefreshComplete();
      }
      @Override
      public void fail(int codeError,Response response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void create() {
    mGalleryManager   = new GalleryManager<Like>(LikesActivity.this,mLikesDataList);
    mLikesGridAdapter = new LikesGridAdapter(LikesActivity.this,mGalleryManager);
    mGallery.getRefreshableView().setAdapter(mLikesGridAdapter);
    mGallery.setOnScrollListener(mGalleryManager);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mGalleryManager!=null) { 
      mGalleryManager.release();
      mGalleryManager=null;
    }
    mGallery=null;
    mLikesGridAdapter=null;
    mLikesDataList=null;
    mProgressDialog=null;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    //mGalleryManager.restart();
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
