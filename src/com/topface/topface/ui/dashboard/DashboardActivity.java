package com.topface.topface.ui.dashboard;

import com.topface.topface.App;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.services.NotificationService;
import com.topface.topface.social.SocialActivity;
import com.topface.topface.ui.LeaksActivity;
import com.topface.topface.ui.LogActivity;
import com.topface.topface.ui.dating.DatingActivity;
import com.topface.topface.ui.inbox.InboxActivity;
import com.topface.topface.ui.likes.LikesActivity;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.rates.RatesActivity;
import com.topface.topface.ui.tops.TopsActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.Imager;
import com.topface.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 *  Класс главного активити для навигации по приложению  "TopFace"
 */
public class DashboardActivity extends Activity implements ServiceConnection, View.OnClickListener {
  // Data
  private TextView mLikesNotify;
  private TextView mInboxNotify;
  private TextView mRatesNotify;
  private ProgressDialog mProgressDialog;
  // Notification Reciever 
  private NotificationReceiver mNotificationReceiver;
  // Notification Service
  private Messenger mNotificationService;
  // Constants
  public static final int INTENT_DASHBOARD = 100;
  public static final String BROADCAST_ACTION = "com.topface.topface.DASHBOARD_NOTIFICATION";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    if(App.init && Data.SSID!=null && Data.SSID.length() > 0) {
      // Progress Bar
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setMessage(getString(R.string.dialog_loading));
      
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
      
      bindService(new Intent(this,NotificationService.class),this,Context.BIND_AUTO_CREATE);
      
      mProgressDialog.show();
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
    
    if(Data.SSID!=null && Data.SSID.length() > 0) {
      // start acceleration 
      if(mNotificationService != null)
        try {
          mNotificationService.send(Message.obtain(null, NotificationService.MSG_ACCEL,0,0));
        } catch(RemoteException e) {
          Debug.log(this,"onStop:remote exception");
        }
      
      // stop broadcaster
      if(mNotificationReceiver == null) {
        mNotificationReceiver = new NotificationReceiver();
        registerReceiver(mNotificationReceiver,new IntentFilter(BROADCAST_ACTION));
      }
      
      invalidateNotification();
      updateNotify();
    } else {
      startActivity(new Intent(getApplicationContext(),SocialActivity.class));
      finish();
    }
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
    // stop acceleration    
    if(mNotificationService != null)
      try {
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_DEACCEL,0,0));
      } catch(RemoteException e) {
        Debug.log(this,"onStop:remote exception");
      }
    
    // stop broadcaster
    if(mNotificationReceiver != null) {
      unregisterReceiver(mNotificationReceiver);
      mNotificationReceiver = null;
    }

    super.onStop();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    System.gc();
    
    if(mProgressDialog!=null && mProgressDialog.isShowing())
      mProgressDialog.cancel();
    mProgressDialog = null;
    
    unbindService(this);
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void update() {
    ProfileRequest profileRequest = new ProfileRequest(this,false);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final ApiResponse response) {
        Profile profile = Profile.parse(response,false);
        Data.setProfile(profile);
        mProgressDialog.cancel();
        new Thread(new Runnable() {
          @Override
          public void run() {
            Imager.avatarOwnerPreloading(DashboardActivity.this.getApplicationContext());
          }
        }).start();
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
  // Overriden from ServiceConnection
  //---------------------------------------------------------------------------
  @Override
  public void onServiceConnected(ComponentName name,IBinder service) {
    try {
      if(mNotificationService == null)
        mNotificationService = new Messenger(service);
    } catch (Exception e) {
      Debug.log("App","onServiceConnected:"+e);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onServiceDisconnected(ComponentName name) {
    try {
      mNotificationService = null;
    } catch (Exception e) {
      Debug.log("App","onServiceDisconnected:"+e);
    }
  }
  //---------------------------------------------------------------------------
  // class NotificationReceiver
  //---------------------------------------------------------------------------
  public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(BROADCAST_ACTION)) {
        DashboardActivity.this.invalidateNotification();
      }
    }
  }
  //---------------------------------------------------------------------------
}
