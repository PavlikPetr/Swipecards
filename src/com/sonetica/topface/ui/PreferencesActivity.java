package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import com.sonetica.topface.R.layout;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.social.SocialWebActivity;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/*
 *    Класс активити для отображения настроек приложения
 */
public class PreferencesActivity extends PreferenceActivity {
  // Data
  public static final int INTENT_PREFERENCES_ACTIVITY = 104;
  //---------------------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Debug.log(this,"+onCreate");
    
    addPreferencesFromResource(R.layout.ac_preferences);
    
    // Social Button
    findPreference("pref_login").setOnPreferenceClickListener(
      new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
          SharedPreferences preferences = getSharedPreferences(AuthToken.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
          String sn = preferences.getString(AuthToken.KEY_SOCIAL_NETWORK,"");
          if(sn.length()>0) {
            Intent intent = new Intent(PreferencesActivity.this, SocialWebActivity.class);
            if(sn.equals(AuthToken.SN_VKONTAKTE))
              intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_VKONTAKTE);
            if(sn.equals(AuthToken.SN_FACEBOOK))
              intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_FACEBOOK);
            startActivityForResult(intent,SocialWebActivity.INTENT_SOCIAL_WEB);
          }
          return true;
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
