package com.sonetica.topface;

import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/*
 *       "оценка фото"
 */
public class PhotoratingActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_photorating);
    Debug.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.photorating_header_title));
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
