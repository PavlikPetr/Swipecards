package com.sonetica.topface;

import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_profile);
    Utils.log(this,"+onCreate");
    // Title Header
    
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.profile_header_title));
  }
  //---------------------------------------------------------------------------
}
