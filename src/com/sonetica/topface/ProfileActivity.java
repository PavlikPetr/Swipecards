package com.sonetica.topface;

import android.os.Handler;
import android.os.Message;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/*
 *                   "Профиль"
 */
public class ProfileActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_profile);
    Debug.log(this,"+onCreate");
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.profile_header_title));
    
    // TextView Profile   
    final TextView tvProfile = ((TextView)findViewById(R.id.tvProfile));
    
    ProfileRequest profileRequest = new ProfileRequest();
    ConnectionService.sendRequest(profileRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        tvProfile.setText(resp.getProfile());
      }
    });
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
