package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class BuyingActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_buying);
    Debug.log(this,"+onCreate");
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.buying_header_title));
  }
//---------------------------------------------------------------------------
}
