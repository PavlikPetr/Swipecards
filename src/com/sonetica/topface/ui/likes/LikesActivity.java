package com.sonetica.topface.ui.likes;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.LikesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.ui.tops.RateitActivity;
import com.sonetica.topface.ui.tops.TopsActivity;
import com.sonetica.topface.ui.tops.TopsGridAdapter;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.GalleryCachedManager;
import com.sonetica.topface.utils.GalleryManager;
import com.sonetica.topface.utils.IFrame;
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

/*
 *      "я нравлюсь"
 */
public class LikesActivity extends Activity {
  // Data
  private GridView mGallery;
  private ProgressDialog mProgressDialog;
  private GalleryManager mGalleryManager;
  private LikesGridAdapter mLikesGridAdapter;
  private ArrayList<String> mLikesList;
  private ArrayList<Like> mLikes;
  private int m_city = PITER;
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
   btnGirls.setText(getString(R.string.tops_btn_girls));
   btnGirls.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       m_city = PITER;
       update();
     }
   });
   
   // Boy Button
   Button btnBoys  = (Button)findViewById(R.id.btnBarBoys);
   btnBoys.setText(getString(R.string.tops_btn_boys));
   btnBoys.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       m_city = PITER;
       update();
     }
   });
   
   // City Button
   Button btnCity  = (Button)findViewById(R.id.btnBarCity);
   btnCity.setText(getString(R.string.tops_btn_city));
   btnCity.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       
       // Смена города
       m_city = 1;
       update();
     }
   });
   
   // Gallery
   mGallery = (GridView)findViewById(R.id.grdLikesGallary);
   mGallery.setAnimationCacheEnabled(false);
   mGallery.setScrollingCacheEnabled(false);
   mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(LikesActivity.this,RateitActivity.class);
       intent.putExtra(RateitActivity.INTENT_USER_ID,mLikes.get(position).uid);
       startActivity(intent);
     }
   });
   
   // Progress Bar
   mProgressDialog = new ProgressDialog(this);
   mProgressDialog.setMessage(getString(R.string.dialog_loading));
   mProgressDialog.show();
   
   update();
  }
  //---------------------------------------------------------------------------
  private void update() {
    LikesRequest likesRequest = new LikesRequest();
    likesRequest.offset = 0;
    likesRequest.limit  = 20;
    ConnectionService.sendRequest(likesRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        mLikesList = new ArrayList<String>();
        mLikes = resp.getLikes();
        for(Like like : mLikes)
          mLikesList.add(like.avatars_small);
        if(mLikesList != null) {
          mGalleryManager   = new GalleryManager(LikesActivity.this,mLikesList,4);
          mLikesGridAdapter = new LikesGridAdapter(LikesActivity.this,mGalleryManager);
          mGallery.setAdapter(mLikesGridAdapter);
        }
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
