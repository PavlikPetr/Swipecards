package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.MainRequest;
import com.sonetica.topface.net.PhotoDeleteRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.album.AlbumGallery;
import com.sonetica.topface.ui.album.AlbumGalleryAdapter;
import com.sonetica.topface.ui.album.AlbumGalleryManager;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoAlbumActivity extends Activity {
  // Data
  private boolean mOwner;
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
    
    mOwner = getIntent().getBooleanExtra(INTENT_OWNER,false);
    
    int uid = getIntent().getIntExtra(INTENT_USER_ID,-1);
    int position = getIntent().getIntExtra(INTENT_ALBUM_POS,0);

    if(uid==-1) {
      Debug.log(this,"Intent param is wrong");
      finish();      
    }
    
    // Gallery
    mGallery = (AlbumGallery)findViewById(R.id.galleryAlbum);
    mGalleryManager = new AlbumGalleryManager(getApplicationContext(),mAlbumsList);
    mGalleryAdapter = new AlbumGalleryAdapter(getApplicationContext(),mGalleryManager);
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
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_MAIN = 0;
  private static final int MENU_DELETE = 1;
  @Override
  public boolean onCreatePanelMenu(int featureId,Menu menu) {
    if(mOwner) {
      menu.add(0,MENU_MAIN,0,getString(R.string.album_menu_main));
      menu.add(0,MENU_DELETE,0,getString(R.string.album_menu_delete));
    }
    return super.onCreatePanelMenu(featureId,menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch(item.getItemId()) {
      case MENU_MAIN: {
        MainRequest request = new MainRequest(getApplicationContext());
        request.photoid = mAlbumsList.get(mGallery.getSelectedItemPosition()).id;
        request.callback(new ApiHandler() {
          @Override
          public void success(Response response) {
            Toast.makeText(PhotoAlbumActivity.this,getString(R.string.album_menu_did_main),Toast.LENGTH_SHORT).show();
          }
          @Override
          public void fail(int codeError,Response response) {
          }
        }).exec();
      } break;
      case MENU_DELETE: {
        PhotoDeleteRequest request = new PhotoDeleteRequest(getApplicationContext());
        request.photoid = mAlbumsList.get(mGallery.getSelectedItemPosition()).id;
        request.callback(new ApiHandler() {
          @Override
          public void success(Response response) {
            Toast.makeText(PhotoAlbumActivity.this,getString(R.string.album_menu_did_delete),Toast.LENGTH_SHORT).show();
          }
          @Override
          public void fail(int codeError,Response response) {
          }
        }).exec();
      } break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
}
