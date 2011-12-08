package com.sonetica.topface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import com.sonetica.topface.net.Profiles;
import com.sonetica.topface.net.Requester;
import com.sonetica.topface.net.Top;
import com.sonetica.topface.net.Profile;
import com.sonetica.topface.utils.Utils;
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
    Utils.log(this,"+onCreate");
    // Title Header
    
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.profile_header_title));

    SharedPreferences preferences = getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    final String ssid = preferences.getString(getString(R.string.ssid),"");

    /*
    Profile profile = new Profile();
    profile.ssid = ssid;
    Requester.sendProfile(profile,
      new Handler() {
        @Override
        public void handleMessage(Message msg) {
          super.handleMessage(msg);
          if (msg.arg1 == Requester.OK && msg.obj != null) {

            int i = 0;

          }
        }
      }
    );
    */

    Profiles profile = new Profiles();
    profile.uids.add(9626403);
    profile.uids.add(1504738);
    profile.uids.add(7756978);
    profile.uids.add(5963313);
    profile.uids.add(17201895);
    profile.ssid = ssid;
    Requester.sendProfiles(profile,
      new Handler() {
        @Override
        public void handleMessage(Message msg) {
          super.handleMessage(msg);
          if (msg.arg1 == Requester.OK && msg.obj != null) {

            int i = 0;

          }
        }
      }
    );
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
