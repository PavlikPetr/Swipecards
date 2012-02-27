package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;

public class AddPhotoActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_add_photo);
    Debug.log(this,"+onCreate");
    
  }
  //---------------------------------------------------------------------------
}
