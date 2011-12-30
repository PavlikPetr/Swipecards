package com.sonetica.topface.ui.dashboard;

import java.util.concurrent.ExecutorService;
import com.sonetica.topface.Global;
import com.sonetica.topface.R;
import com.sonetica.topface.PhotoratingActivity;
import com.sonetica.topface.PreferencesActivity;
import com.sonetica.topface.ProfileActivity;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.ui.chat.ChatActivity;
import com.sonetica.topface.ui.likes.LikesActivity;
import com.sonetica.topface.ui.rates.RatesActivity;
import com.sonetica.topface.ui.tops.TopsActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
  private volatile boolean mIsActive;
  private ExecutorService  mThreadsPool;
  private NotifyHandler    mNotifyHandler;
  // Constants
  public static final int INTENT_DASHBOARD = 100;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Debug.log(this,"+onCreate");
    
    ((Button)findViewById(R.id.btnDashbrdPhotorating)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdLikeme)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdMyrating)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdChat)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdTops)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdProfile)).setOnClickListener(this);
    
    if(!Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
    
    mNotifyHandler = new NotifyHandler();
    mNotifyHandler.sendEmptyMessage(0);
    
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    
    if(Global.SSID.length()>0)
      return;

    startActivity(new Intent(this,SocialActivity.class));
    finish();
 }
  //---------------------------------------------------------------------------  
  @Override
  protected void onResume() {
    super.onResume();
    mIsActive = true;
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
        startActivity(new Intent(this,LikesActivity.class));
      } break;
      case R.id.btnDashbrdMyrating: {
        startActivity(new Intent(this,RatesActivity.class));
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
  @Override
  protected void onPause() {
    super.onPause();
    mIsActive = false;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mNotifyHandler = null;
    Debug.log(this,"-onDestroy");
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
  // NotifyHandler
  //---------------------------------------------------------------------------
  class NotifyHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if(mNotifyHandler==null)
        return;
      if(!mIsActive)
        this.sendEmptyMessageDelayed(0,1000*3);
      else {
        ProfileRequest profileRequest = new ProfileRequest(true);
        ConnectionService.sendRequest(profileRequest,new Handler() {
          @Override
          public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final Response resp = (Response)msg.obj;
            final Activity context = DashboardActivity.this;
            if(context!=null)
              context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  String s = resp.getProfile();
                  Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
                  NotifyHandler.this.sendEmptyMessageDelayed(0,1000*3);
                }
              });
          }
        });
      }
    }
  }
  //---------------------------------------------------------------------------
}
