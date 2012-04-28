package com.topface.topface.ui;

import android.app.Activity;

/*
 * Класс стартового активити для показа прелоадера и инициализации данных
 */
public class MainActivity extends Activity {} /*
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_main);
    Debug.log(this,"+onCreate");

    LeaksManager.getInstance().monitorObject(this);
  
    Intent intent = null;
    if(App.SSID!=null && App.SSID.length()>0) {
      intent = new Intent(getApplicationContext(), DashboardActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    } else
      intent = new Intent(getApplicationContext(), SocialActivity.class);
    startActivity(intent);
    
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
*/
// onActivityResult should be called after onStart and before onResume.
/*
  onCreate
  onStart
  onRestoreInstanceState
  onActivityResult
  onResume
*/