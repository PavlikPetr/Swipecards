package com.sonetica.topface;

import com.sonetica.topface.services.StatisticService;
import com.sonetica.topface.ui.dashboard.DashboardActivity;
import com.sonetica.topface.utils.Utils;
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
    Utils.log(this,"+onCreate");
    
    //start Dashboard Activity
    startActivity(new Intent(this,DashboardActivity.class));
    
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