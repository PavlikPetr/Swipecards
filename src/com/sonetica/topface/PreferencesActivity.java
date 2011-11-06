package com.sonetica.topface;

import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.utils.Utils;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/*
 * Класс активити для отображения настроек приложения
 */
public class PreferencesActivity extends PreferenceActivity {
  //---------------------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.layout.ac_preferences);
    Utils.log(this,"+onCreate");
    
    // Social Button
    findPreference("pref_login").setOnPreferenceClickListener(
      new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
          startActivity(new Intent(PreferencesActivity.this, SocialActivity.class));
          return true;
        }
      }
    );
    
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    super.onDestroy();
    Utils.log(this,"-onDestroy");
  }
  //---------------------------------------------------------------------------
}
