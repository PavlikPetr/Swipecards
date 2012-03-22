package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.MainRequest;
import com.sonetica.topface.net.PhotoDeleteRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class PhotoAlbumActivity extends Activity {
  // Data
  private boolean mOwner;
  private TextView mCounter;
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
    
    System.gc();
    
    LeaksManager.getInstance().monitorObject(this);

    // Title Header
    mCounter = ((TextView)findViewById(R.id.tvHeaderTitle));
    
    // Progress Dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    mAlbumsList = Data.s_PhotoAlbum;
    
    mOwner = getIntent().getBooleanExtra(INTENT_OWNER,false);
    
    int uid = getIntent().getIntExtra(INTENT_USER_ID,-1);  // нахуя он нужен, разобраться почему это здесь написано!!!
    final int position = getIntent().getIntExtra(INTENT_ALBUM_POS,0);

    if(uid==-1) {
      Debug.log(this,"Intent param is wrong");
      finish();      
    }
    
    // Gallery Adapter
    mGalleryManager = new AlbumGalleryManager(getApplicationContext(),mAlbumsList);
    mGalleryAdapter = new AlbumGalleryAdapter(getApplicationContext(),mGalleryManager);

    // Gallery
    mGallery = (AlbumGallery)findViewById(R.id.galleryAlbum);
    mGallery.setAdapter(mGalleryAdapter);
    mGallery.setSelection(position,true);
    mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0,View arg1,int position,long arg3) {
        PhotoAlbumActivity.this.setCounter(position+1,mAlbumsList.size()); //  УПРАВЛЕНИЕ СЧЕТЧИКОМ
      }
      @Override
      public void onNothingSelected(AdapterView<?> arg0) {}
    });
    
    setCounter(position+1,mAlbumsList.size());
  }
  //---------------------------------------------------------------------------
  // счетчик галереи
  public void setCounter(int index,int size) {
    mCounter.setText(index+"/"+size);
    mCounter.invalidate();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    mAlbumsList = null;
    mGallery = null;
    mGalleryManager.release();
    mGalleryManager = null;
    mGalleryAdapter = null;
    
    System.gc();
    
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
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(PhotoAlbumActivity.this.getString(R.string.album_menu_popup_delete))
               .setCancelable(false)
               .setPositiveButton(PhotoAlbumActivity.this.getString(R.string.album_menu_popup_yes), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                   PhotoAlbumActivity.this.deletePhoto();
                 }
               })
               .setNegativeButton(PhotoAlbumActivity.this.getString(R.string.album_menu_popup_no), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                   dialog.cancel();
                 }
               });
        AlertDialog alert = builder.create();
        alert.show();

      } break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
  private void deletePhoto() {
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
  }
  //---------------------------------------------------------------------------
}
