package com.sonetica.topface.ui.likes;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.net.LikesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.ui.GalleryManager;
import com.sonetica.topface.ui.PullToRefreshGridView;
import com.sonetica.topface.ui.PullToRefreshBase.OnRefreshListener;
import com.sonetica.topface.ui.album.AlbumActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/*
 *      "я нравлюсь"
 */
public class LikesActivity extends Activity {
  // Data
  private PullToRefreshGridView mGallery;
  private ProgressDialog mProgressDialog;
  private GalleryManager mGalleryManager;
  private LikesGridAdapter mLikesGridAdapter;
  private ArrayList<Like> mLikes;
  // Constats
  private static int PITER = 2;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likes);
    Debug.log(this,"+onCreate");
    
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
   mGallery.getAdapterView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(LikesActivity.this,AlbumActivity.class);
       intent.putExtra(AlbumActivity.INTENT_USER_ID,mLikes.get(position).uid);
       startActivity(intent);
     }
   });
      
   mGallery.setOnRefreshListener(new OnRefreshListener() {
     @Override
     public void onRefresh() {
       update();
     }});
   
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   
   update();
  }
  //---------------------------------------------------------------------------
  private void update() {
    mProgressDialog.show();
    
    LikesRequest likesRequest = new LikesRequest();
    likesRequest.offset = 0;
    likesRequest.limit  = 20;
    ConnectionService.sendRequest(likesRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        mLikes = resp.getLikes();
        if(mLikes != null) {
          mGalleryManager   = new GalleryManager(LikesActivity.this,mLikes);
          mLikesGridAdapter = new LikesGridAdapter(LikesActivity.this,mGalleryManager);
          mGallery.getAdapterView().setAdapter(mLikesGridAdapter);
        }
        mGallery.onRefreshComplete();
        mProgressDialog.cancel();
      }
    });
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
