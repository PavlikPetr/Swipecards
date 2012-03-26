package com.sonetica.topface.ui.dashboard;

import com.sonetica.topface.App;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.ui.LogActivity;
import com.sonetica.topface.ui.LeaksActivity;
import com.sonetica.topface.ui.dating.DatingActivity;
import com.sonetica.topface.ui.inbox.InboxActivity;
import com.sonetica.topface.ui.likes.LikesActivity;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.ui.rates.RatesActivity;
import com.sonetica.topface.ui.tops.TopsActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.Imager;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 *  Класс главного активити для навигации по приложению  "TopFace"
 */
public class DashboardActivity extends Activity implements View.OnClickListener {
  // Data
  private boolean start;
  private TextView mLikesNotify;
  private TextView mInboxNotify;
  private TextView mRatesNotify;
  private ProgressDialog mProgressDialog;
  // Notification
  //private Handler mNotifyHandler;
  //private Intent mNotificationRecieverIntent;
  private NotificationReceiver mNotificationReceiver;
  // Constants
  //private static final long TIMER = 1000L * 15;
  public  static final int INTENT_DASHBOARD = 100;
  public  static final String ACTION = "com.sonetica.topface.DASHBOARD_NOTIFICATION";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    if(App.init && Data.SSID.length() > 0) {
      //startService(new Intent(this,ConnectionService.class));
      //startService(new Intent(getApplicationContext(),StatisticService.class));
      //Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
      //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
      //startActivity(intent);
      //mNotifyHandler.sendEmptyMessageDelayed(0,sleep_time);
      
      // Progress Bar
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setMessage(getString(R.string.dialog_loading));
      mProgressDialog.show();
      
      mLikesNotify = (TextView)findViewById(R.id.tvDshbrdNotifyLikes);
      mInboxNotify = (TextView)findViewById(R.id.tvDshbrdNotifyChat);
      mRatesNotify = (TextView)findViewById(R.id.tvDshbrdNotifyRates);
  
      ((Button)findViewById(R.id.btnDshbrdDating)).setOnClickListener(this);
      ((Button)findViewById(R.id.btnDshbrdLikes)).setOnClickListener(this);
      ((Button)findViewById(R.id.btnDshbrdRates)).setOnClickListener(this);
      ((Button)findViewById(R.id.btnDshbrdChat)).setOnClickListener(this);
      ((Button)findViewById(R.id.btnDshbrdTops)).setOnClickListener(this);
      ((Button)findViewById(R.id.btnDshbrdProfile)).setOnClickListener(this);
      
      Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.im_bar_header);
      Data.s_HeaderHeight = bitmap.getHeight();
      bitmap.recycle();
      
      if(!Http.isOnline(this)){
        Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
        return;
      }
      
      //mNotifyHandler = new Handler();
      //mNotifyHandler.postDelayed(new RunTask(),TIMER);
      
      update();
    } else { 
      startActivity(new Intent(getApplicationContext(),SocialActivity.class));
      finish();
    }
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    System.gc();
    //App.bind(getBaseContext());
    
    if(start && Data.SSID.length() > 0) {
      
      if(mNotificationReceiver == null) {
        mNotificationReceiver = new NotificationReceiver();
        registerReceiver(mNotificationReceiver,new IntentFilter(ACTION));
      }
      
      invalidateNotification();
      updateNotify();
      return;
    }
    startActivity(new Intent(getApplicationContext(),SocialActivity.class));
    finish();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
    //App.unbind();
    
    if(mNotificationReceiver != null) {
      unregisterReceiver(mNotificationReceiver);
      mNotificationReceiver = null;
    }

    super.onStop();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    start = false;
    System.gc();
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void update() {
    start = true;
    
    ProfileRequest profileRequest = new ProfileRequest(this,false);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final ApiResponse response) {
        Profile profile = Profile.parse(response,false);
        Data.setProfile(profile);
        mProgressDialog.cancel();
        Imager.avatarOwnerPreloading(DashboardActivity.this.getApplicationContext());
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void updateNotify() {
    ProfileRequest profileRequest = new ProfileRequest(getApplicationContext(),true);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final ApiResponse response) {
        Profile profile = Profile.parse(response,true);
        Data.updateNotification(profile);
        invalidateNotification();
        Debug.log(DashboardActivity.this,"up");
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void invalidateNotification() {
    if(Data.s_Likes > 0) {
      mLikesNotify.setText(" "+Data.s_Likes+" ");
      mLikesNotify.setVisibility(View.VISIBLE);
    } else
      mLikesNotify.setVisibility(View.INVISIBLE);

    if(Data.s_Messages > 0) {
      mInboxNotify.setText(" "+Data.s_Messages+" ");
      mInboxNotify.setVisibility(View.VISIBLE);
    } else
      mInboxNotify.setVisibility(View.INVISIBLE);
    
    if(Data.s_Rates > 0) {
      mRatesNotify.setText(" "+Data.s_Rates+" ");
      mRatesNotify.setVisibility(View.VISIBLE);
    } else
      mRatesNotify.setVisibility(View.INVISIBLE);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {  
    if(!Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }

    switch(view.getId()) {
      case R.id.btnDshbrdDating: {
        startActivity(new Intent(this.getApplicationContext(),DatingActivity.class));
      } break;
      case R.id.btnDshbrdLikes: {
        startActivity(new Intent(this.getApplicationContext(),LikesActivity.class));
      } break;
      case R.id.btnDshbrdRates: {
        startActivity(new Intent(this.getApplicationContext(),RatesActivity.class));
      } break;
      case R.id.btnDshbrdChat: {
        startActivity(new Intent(this.getApplicationContext(),InboxActivity.class));
      } break;
      case R.id.btnDshbrdTops: {
        startActivity(new Intent(this.getApplicationContext(),TopsActivity.class));
      } break;
      case R.id.btnDshbrdProfile: {
        startActivity(new Intent(this.getApplicationContext(),ProfileActivity.class));
      } break;      
      default:
    }
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_LOG = 2;
  private static final int MENU_LEAKS = 3;
  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    menu.add(0,MENU_LOG,0,"Log");
    menu.add(0,MENU_LEAKS,0,"Leaks");
    
    return super.onCreatePanelMenu(featureId, menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch(item.getItemId()) {
      case MENU_LOG:
        startActivity(new Intent(getApplicationContext(),LogActivity.class));
        break;
      case MENU_LEAKS:
        startActivity(new Intent(getApplicationContext(),LeaksActivity.class));
        break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
  // class RunTask
  //---------------------------------------------------------------------------
//  class RunTask implements Runnable {
//    public void run() {
//      invalidateNotification();
//      mNotifyHandler.postDelayed(this,TIMER);
//    }
//  }
  //---------------------------------------------------------------------------
  // class NotificationReceiver
  //---------------------------------------------------------------------------
  public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(ACTION)) {
        invalidateNotification();
      }
    }
  }
  //---------------------------------------------------------------------------
}
