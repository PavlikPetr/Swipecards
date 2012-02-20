package com.sonetica.topface.ui.album;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.net.AlbumRequest;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

public class AlbumActivity extends Activity {
  // Data
  private AlbumGallery  mGallery;
  private LinkedList<Album> mAlbumsList;
  private ProgressDialog mProgressDialog;
  private AlbumGalleryManager mGalleryManager;
  private AlbumGalleryAdapter mGalleryAdapter;
  // Constants
  public static final String INTENT_USER_ID = "user_id";
  public static final String INTENT_ALBUM_POS = "album_position";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_album);
    Debug.log(this,"+onCreate");

    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.album_header_title));
    
    // Progress Dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    // Gallery
    mGallery = (AlbumGallery)findViewById(R.id.galleryAlbum);
    
    mAlbumsList = new LinkedList<Album>();
   
    int uid = getIntent().getIntExtra(INTENT_USER_ID,-1);
    int pos = getIntent().getIntExtra(INTENT_ALBUM_POS,0);
    if(uid==-1) {
      Debug.log(this,"Intent param is wrong");
      finish();      
    }
    update(uid,pos);
  }
  //---------------------------------------------------------------------------
  private void update(int uid,final int position) {
    AlbumRequest albumRequest = new AlbumRequest(this);
    albumRequest.uid  = uid;
    albumRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        mAlbumsList = Album.parse(response); 
        mGalleryManager = new AlbumGalleryManager(AlbumActivity.this,mAlbumsList);
        mGalleryAdapter = new AlbumGalleryAdapter(AlbumActivity.this,mGalleryManager);
        mGallery.setAdapter(mGalleryAdapter);
        mGallery.setSelection(position,true);
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
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
