package com.sonetica.topface;

import com.sonetica.topface.services.StatisticService;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.ui.dashboard.DashboardActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
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

    LeaksManager.getInstance().monitorObject(this);
    
    //startService(new Intent(this,ConnectionService.class));
    startService(new Intent(getApplicationContext(),StatisticService.class));
    
    if(Data.SSID.length()>0) {
      Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
      startActivity(intent);
    } else
      startActivity(new Intent(getApplicationContext(),SocialActivity.class));
    
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