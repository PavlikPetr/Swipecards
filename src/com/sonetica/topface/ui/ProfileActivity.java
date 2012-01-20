package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

/*
 *      "Профиль"
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
    tvProfile.setTextColor(Color.WHITE);
    
    ProfileRequest profileRequest = new ProfileRequest(this,false);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {
        ProfileActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            String s = response.getProfile();
            tvProfile.setText(s);
            tvProfile.invalidate();
          }
        });
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
