package com.sonetica.topface;

import com.sonetica.topface.R;
import com.sonetica.topface.R.id;
import com.sonetica.topface.R.layout;
import com.sonetica.topface.R.string;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/*
 *      "я нравлюсь"
 */
public class LikemeActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_likeme);
    Utils.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.likeme_header_title));
  }
  //---------------------------------------------------------------------------
}
