package com.sonetica.topface.ui.tops;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.net.AlbumRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.GalleryManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class RateitActivity extends Activity {
  // Data
  private RateitGallery  mGallery;
  private ProgressDialog mProgressDialog;
  private GalleryManager mGalleryManager;
  private RateitGalleryAdapter mGalleryAdapter;
  private ArrayList<String> mLinkList;
  // Constants
  public static final String INTENT_USER_ID = "user_id";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_rateit2);
    Debug.log(this,"+onCreate");

    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.rateit_header_title));
    
    // Progress Dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    // Gallery
    mGallery = (RateitGallery)findViewById(R.id.galleryRateit2);
    
    mLinkList = new ArrayList<String>();
   
    try {
      int uid = Integer.parseInt(getIntent().getStringExtra(INTENT_USER_ID));
      update(uid);
    } catch(Exception e) {
      finish();      
    }
  }
  //---------------------------------------------------------------------------
  private void update(int uid) {
    AlbumRequest albumRequest = new AlbumRequest();
    albumRequest.uid  = uid;
    ConnectionService.sendRequest(albumRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        mLinkList = resp.getAlbum();
        mGalleryManager = new GalleryManager(RateitActivity.this,mLinkList,1);
        mGalleryAdapter = new RateitGalleryAdapter(RateitActivity.this,mGalleryManager);
        mGallery.setAdapter(mGalleryAdapter);
        mProgressDialog.cancel();
      }
    });
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    
    if(mGalleryManager!=null) {
      mGalleryManager.release();
      mGalleryManager = null;
    }
    if(mGalleryAdapter!=null) mGalleryAdapter=null;
    if(mGallery!=null)        mGallery=null;
    
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
}

/*
ProfilesRequest profilesRequest = new ProfilesRequest();
profilesRequest.uids.add(uid);
ConnectionService.sendRequest(profilesRequest,new Handler() {
  @Override
  public void handleMessage(Message msg) {
    super.handleMessage(msg);
    Response resp = (Response)msg.obj;
    // do it

    mProgressDialog.cancel();
  }
});
*/