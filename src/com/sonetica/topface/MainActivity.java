package com.sonetica.topface;

import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.ui.dashboard.DashboardActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
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
    Debug.log(this,"+onCreate");
    
    //startService(new Intent(this,ConnectionService.class));
    //startService(new Intent(this,StatisticService.class));
    
    if(Data.SSID.length()>0)
      startActivity(new Intent(this,DashboardActivity.class));
    else
      startActivity(new Intent(this,SocialActivity.class));
    
    finish();    
    
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}

// onActivityResult should be called after onStart and before onResume.
/*
  onCreate
  onStart
  onRestoreInstanceState
  onActivityResult
  onResume
*/