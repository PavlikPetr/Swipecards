package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;

public class FilterActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_filter);
    Debug.log(this,"+onCreate");
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
