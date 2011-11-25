package com.sonetica.topface;

import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/*
 * Класс стартового активити для показа прелоадера и инициализации данных
 * (вывел данное активити из оборота)
 */
public class MainActivity extends Activity {
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_main);
    Utils.log(this,"+onCreate");

    //start Dashboard Activity
    //startActivityForResult(new Intent(this,DashboardActivity.class),DashboardActivity.INTENT_DASHBOARD);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    //if(requestCode == DashboardActivity.INTENT_DASHBOARD)
      //finish();
  }  
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}