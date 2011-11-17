package com.sonetica.topface;

import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ChatActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_chat);
    Utils.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.chat_header_title));
  }
  //---------------------------------------------------------------------------
}
