package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.ui.album.AlbumGallery;
import com.sonetica.topface.ui.album.AlbumGalleryAdapter;
import com.sonetica.topface.ui.album.AlbumGalleryManager;
import com.sonetica.topface.ui.dashboard.DashboardActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

public class PhotoAlbumActivity extends Activity {
  // Data
  private AlbumGallery  mGallery;
  private LinkedList<Album> mAlbumsList;
  private ProgressDialog mProgressDialog;
  private AlbumGalleryManager mGalleryManager;
  private AlbumGalleryAdapter mGalleryAdapter;
  // Constants
  public static final String INTENT_OWNER = "owner";
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
    
    mAlbumsList = Data.s_PhotoAlbum;
    
    int uid = getIntent().getIntExtra(INTENT_USER_ID,-1);
    int position = getIntent().getIntExtra(INTENT_ALBUM_POS,0);

    if(uid==-1) {
      Debug.log(this,"Intent param is wrong");
      finish();      
    }
    
    // Gallery
    mGallery = (AlbumGallery)findViewById(R.id.galleryAlbum);
    mGalleryManager = new AlbumGalleryManager(this.getApplicationContext(),mAlbumsList);
    mGalleryAdapter = new AlbumGalleryAdapter(this.getApplicationContext(),mGalleryManager);
    mGallery.setAdapter(mGalleryAdapter);
    mGallery.setSelection(position,true);
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    mAlbumsList = null;
    mGallery = null;
    mGalleryManager.release();
    mGalleryManager = null;
    mGalleryAdapter = null;
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
}
