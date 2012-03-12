package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import com.sonetica.topface.social.Socium;
import com.sonetica.topface.social.Socium.AuthException;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddPhotoActivity extends Activity {
  // Data
  private Uri mImageUri;
  //private ImageView mImage;
  private ProgressDialog mProgress;
  // Constants
  public static final int GALLARY_IMAGE_ACTIVITY_REQUEST_CODE = 100;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_add_photo);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.album_add_photo_title));

    // progress
    mProgress = new ProgressDialog(this);
    mProgress.setMessage(getString(R.string.dialog_loading));
    
    // Album button
    Button btnPhotoAlbum = (Button)findViewById(R.id.btnAddPhotoAlbum);
    btnPhotoAlbum.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
      }
    });
    
    // Camera button
    Button btnPhotoCamera = (Button)findViewById(R.id.btnAddPhotoCamera);
    btnPhotoCamera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
      }
    });

    //Intent intent = new Intent();
    //intent.setType("image/*");
    //intent.setAction(Intent.ACTION_GET_CONTENT);
    //intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    //intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    //startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
    
  }
  //---------------------------------------------------------------------------
  // получение фото из галереи и отправка на сервер
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode,resultCode,data);
    if(requestCode == GALLARY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
      mImageUri = data != null ? data.getData() : null;
      if(mImageUri==null)
        return;
      
      new AsyncTaskUploader().execute(mImageUri);
      
    } else {
      Toast.makeText(getApplicationContext(),"oops", Toast.LENGTH_SHORT).show();
    }
  }
  //---------------------------------------------------------------------------
  // class AsyncTaskUploader
  //---------------------------------------------------------------------------
  class AsyncTaskUploader extends AsyncTask<Uri, Void, Void> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgress.show();
    }
    @Override
    protected Void doInBackground(Uri... uri) {
      Socium soc;
      try {
        soc = new Socium(AddPhotoActivity.this.getApplicationContext());
        soc.uploadPhoto(uri[0]);
      } catch(AuthException e) {
        e.printStackTrace();
      }
      return null;
    }
    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      mProgress.cancel();  
    }
  }
  //---------------------------------------------------------------------------  
}

