package com.sonetica.topface;

import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.ui.dashboard.DashboardActivity;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/*
 * Класс стартового активити для показа прелоадера и инициализации данных
 */
public class MainActivity extends Activity {
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_main);
    Utils.log(this,"+onCreate");
    
    SharedPreferences preferences = getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    String ssid = preferences.getString(getString(R.string.ssid),"");

    if(ssid.length()>0)
      startActivity(new Intent(this,DashboardActivity.class));
    else
      startActivity(new Intent(this,SocialActivity.class));
    
    finish();    
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}

//onActivityResult should be called after onStart and before onResume.
/*
onCreate
onStart
onRestoreInstanceState
onActivityResult
onResume
*/