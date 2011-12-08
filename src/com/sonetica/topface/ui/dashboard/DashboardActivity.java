package com.sonetica.topface.ui.dashboard;

import com.sonetica.topface.App;
import com.sonetica.topface.LikemeActivity;
import com.sonetica.topface.R;
import com.sonetica.topface.PhotoratingActivity;
import com.sonetica.topface.PreferencesActivity;
import com.sonetica.topface.ProfileActivity;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.services.StatisticService;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.ui.chat.ChatActivity;
import com.sonetica.topface.ui.myrating.MyratingActivity;
import com.sonetica.topface.ui.tops.TopsActivity;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
 * Класс главного активити для навигации по приложению  "TopFace"
 */
public class DashboardActivity extends Activity implements View.OnClickListener {
  // Data
  private Intent mServiceIntent;
  // Constants
  public static final int INTENT_DASHBOARD = 100;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Utils.log(this,"+onCreate");
    
    if(!Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
    
    startService(mServiceIntent = new Intent(this,StatisticService.class));
    
    ((Button)findViewById(R.id.btnDashbrdPhotorating)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdLikeme)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdMyrating)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdChat)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdTops)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdProfile)).setOnClickListener(this);

  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    
    if(isRegistered())
      return;
    
    startActivity(new Intent(this,SocialActivity.class));
    finish();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {  
    if(!Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
    switch(view.getId()) {
      case R.id.btnDashbrdPhotorating: {
        startActivity(new Intent(this,PhotoratingActivity.class));
      } break;
      case R.id.btnDashbrdLikeme: {
        startActivity(new Intent(this,LikemeActivity.class));
      } break;
      case R.id.btnDashbrdMyrating: {
        startActivity(new Intent(this,MyratingActivity.class));
      } break;
      case R.id.btnDashbrdChat: {
        startActivity(new Intent(this,ChatActivity.class));
      } break;
      case R.id.btnDashbrdTops: {
        startActivity(new Intent(this,TopsActivity.class));
      } break;
      case R.id.btnDashbrdProfile: {
        startActivity(new Intent(this,ProfileActivity.class));
      } break;      
      default:
    }
  }
  //---------------------------------------------------------------------------
  private boolean isRegistered() {
    SharedPreferences preferences = getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    String ssid = preferences.getString(getString(R.string.ssid),"");
    return ssid.length()>0;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    stopService(mServiceIntent);
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_ONE = 0;
  private static final int MENU_PREFERENCES = 1;
  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    menu.add(0,MENU_ONE,0,getString(R.string.dashbrd_menu_one));
    menu.add(0,MENU_PREFERENCES,0,getString(R.string.dashbrd_menu_preferences));
    return super.onCreatePanelMenu(featureId, menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ONE:
        Toast.makeText(this,getString(R.string.dashbrd_menu_one),Toast.LENGTH_SHORT).show();
        break;
      case MENU_PREFERENCES:
        startActivity(new Intent(this,PreferencesActivity.class));
        break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
}
//Toast.makeText(this,"tops",Toast.LENGTH_SHORT).show();