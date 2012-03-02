package com.sonetica.topface.ui.profile;

import java.io.FileNotFoundException;
import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddPhotoActivity extends Activity {
  // Data
  private ImageView mImage;
  // Constants
  public static final int GALLARY_IMAGE_ACTIVITY_REQUEST_CODE = 100;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_add_photo);
    Debug.log(this,"+onCreate");
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.album_add_photo_title));
    
    mImage = (ImageView)findViewById(R.id.ivAddPhotoView);
    mImage.setImageResource(R.drawable.icon_people);  

    Intent intent = new Intent();
    //intent.setType("image/*");
    //intent.setAction(Intent.ACTION_GET_CONTENT);
    //intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
    
  }
  //---------------------------------------------------------------------------
  // получение фото из галереи и отправка на сервер
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode,resultCode,data);
    final Uri imageUri = data != null ? data.getData() : null;
    if (requestCode == GALLARY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && imageUri != null) {
      /*
      try {
        //OutputStream out = new FileOutputStream(new File(URI.create(imageUri.toString())));
        String s = imageUri.getPath();
        URI u = URI.create(s);
        File f = new File(u);
        InputStream in = new FileInputStream(f);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        mImage.setImageBitmap(bitmap);
      } catch(FileNotFoundException e) {
        e.printStackTrace();
      }
      */
      //mImage.setImageURI(imageUri);
        Uri targetUri = data.getData();
        Bitmap bitmap;
        try {
         bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
         mImage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
         e.printStackTrace();
        }
      Toast.makeText(getApplicationContext(),"yes", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getApplicationContext(),"no", Toast.LENGTH_SHORT).show();
    }
  }
  //---------------------------------------------------------------------------
}

