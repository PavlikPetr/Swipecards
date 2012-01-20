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
import com.sonetica.topface.ui.GalleryManager;
import com.sonetica.topface.ui.album.AlbumActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

/*
 *      "я нравлюсь"
 */
public class LikesActivity extends Activity {
  // Data
  private PullToRefreshGridView mGallery;
  private LikesGridAdapter mLikesGridAdapter;
  private GalleryManager mGalleryManager;
  private LinkedList<Like> mLikesList;
  private ProgressDialog mProgressDialog;
  // Constats
  //private static int PITER = 2;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likes);
    Debug.log(this,"+onCreate");
    
    // Data
    mLikesList = Data.s_LikesList;
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.likes_header_title));
   
   // Girl Button
   Button btnGirls = (Button)findViewById(R.id.btnBarGirls);
   btnGirls.setVisibility(View.INVISIBLE);
   btnGirls.setText(getString(R.string.tops_btn_girls));
   btnGirls.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       Toast.makeText(LikesActivity.this,"Girl",Toast.LENGTH_LONG).show();
       //update();
     }
   });
   
   // Boy Button
   Button btnBoys  = (Button)findViewById(R.id.btnBarBoys);
   btnBoys.setVisibility(View.INVISIBLE);
   btnBoys.setText(getString(R.string.tops_btn_boys));
   btnBoys.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       Toast.makeText(LikesActivity.this,"Boy",Toast.LENGTH_LONG).show();
       //update();
     }
   });
   
   // City Button
   Button btnCity  = (Button)findViewById(R.id.btnBarCity);
   btnCity.setVisibility(View.INVISIBLE);
   btnCity.setText(getString(R.string.tops_btn_city));
   btnCity.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       Toast.makeText(LikesActivity.this,"City",Toast.LENGTH_LONG).show();
       //update();
     }
   });
   
   // Gallery
   mGallery = (PullToRefreshGridView)findViewById(R.id.grdLikesGallary);
   mGallery.setAnimationCacheEnabled(false);
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
   mGallery.getAdapterView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(LikesActivity.this,AlbumActivity.class);
       intent.putExtra(AlbumActivity.INTENT_USER_ID,mLikesList.get(position).uid);
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
   
   if(mLikesList.size()==0)
     update();
   else
     create();
   
   // обнуление информера непросмотренных лайков
   Data.mLikes = 0;
  }
  //---------------------------------------------------------------------------
  private void create() {
    mGalleryManager   = new GalleryManager(LikesActivity.this,mLikesList);
    mLikesGridAdapter = new LikesGridAdapter(LikesActivity.this,mGalleryManager);
    mGallery.getAdapterView().setAdapter(mLikesGridAdapter);
  }
  //---------------------------------------------------------------------------
  private void release() {
    if(mGallery!=null)          mGallery=null;
    if(mLikesGridAdapter!=null) mLikesGridAdapter=null;
    if(mLikesList!=null)        mLikesList=null;
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
        mLikesList.addAll(response.getLikes());
        create();
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
    release();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
