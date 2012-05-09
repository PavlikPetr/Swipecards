package com.topface.topface.ui.dashboard;

import com.topface.topface.App;
import com.topface.topface.C2DMUtils;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.social.SocialActivity;
import com.topface.topface.ui.LeaksActivity;
import com.topface.topface.ui.LogActivity;
import com.topface.topface.ui.dating.DatingActivity;
import com.topface.topface.ui.inbox.InboxActivity;
import com.topface.topface.ui.likes2.Likes2Activity;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.symphaty.SymphatyActivity;
import com.topface.topface.ui.tops.TopsActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.Imager;
import com.topface.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
  private boolean mUpdate;
  private TextView mLikesNotify;
  private TextView mInboxNotify;
  private TextView mSymphatyNotify;
  private ProgressDialog mProgressDialog;
  private NotificationManager mNotificationManager;
  private NotificationReceiver mNotificationReceiver;
  // Constants
  public static final String BROADCAST_ACTION = "com.topface.topface.DASHBOARD_NOTIFICATION";
  //---------------------------------------------------------------------------
  // class NotificationReceiver
  //---------------------------------------------------------------------------
  class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(C2DMUtils.C2DM_NOTIFICATION))
        DashboardActivity.this.refreshNotifications();
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // notifications
    mLikesNotify = (TextView)findViewById(R.id.tvDshbrdNotifyLikes);
    mInboxNotify = (TextView)findViewById(R.id.tvDshbrdNotifyChat);
    mSymphatyNotify = (TextView)findViewById(R.id.tvDshbrdNotifySymphaty);

    // Buttons
    ((Button)findViewById(R.id.btnDshbrdDating)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdLikes)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdSymphaty)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdChat)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdTops)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdProfile)).setOnClickListener(this);
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    // C2DM
    C2DMUtils.init(this);
    
    mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    
    // is online
    if(!Http.isOnline(this))
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    
    if(!Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
    
    if(!App.init && App.SSID==null || App.SSID.length()==0) {
      startActivity(new Intent(getApplicationContext(),SocialActivity.class));
      Data.s_OwnerAvatar = null;
      Data.s_UserAvatar  = null;
      finish();
      return;
    }
    /*
    // start broadcaster
    if(mNotificationReceiver == null) {
      mNotificationReceiver = new NotificationReceiver();
      registerReceiver(mNotificationReceiver,new IntentFilter(BROADCAST_ACTION));
    }
    */
    App.s_Facebook.extendAccessTokenIfNeeded(this, null);
    
    
    // start broadcaster
    if(mNotificationReceiver == null) {
      mNotificationReceiver = new NotificationReceiver();
      registerReceiver(mNotificationReceiver,new IntentFilter(C2DMUtils.C2DM_NOTIFICATION));
    }
    
    //NotificationService.startAcceleration(getApplicationContext());
    
    updateProfile();
    
    System.gc();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
    /*
    // stop broadcaster
    if(mNotificationReceiver != null) {
      unregisterReceiver(mNotificationReceiver);
      mNotificationReceiver = null;
    }
    */
    
    
    // stop broadcaster
    if(mNotificationReceiver != null) {
      unregisterReceiver(mNotificationReceiver);
      mNotificationReceiver = null;
    }

    
    //NotificationService.stopAcceleration(getApplicationContext());
    
    super.onStop();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    if(mProgressDialog!=null && mProgressDialog.isShowing())
      mProgressDialog.cancel();
    mProgressDialog = null;
    
    System.gc();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void updateProfile() {
    ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
    if(!mUpdate) {
      mProgressDialog.show();
      profileRequest.part = ProfileRequest.P_DASHBOARD;
    } else
      profileRequest.part = ProfileRequest.P_NOTIFICATION;
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final ApiResponse response) {
        if(!mUpdate) {
          CacheProfile.setData(Profile.parse(response));
          Imager.avatarOwnerPreloading(getApplicationContext());
          mUpdate = true;
        }
        else
          CacheProfile.updateNotifications(Profile.parse(response));
        refreshNotifications();
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        if(mProgressDialog != null)
          mProgressDialog.cancel();
        mLikesNotify.setVisibility(View.INVISIBLE);
        mInboxNotify.setVisibility(View.INVISIBLE);
        mSymphatyNotify.setVisibility(View.INVISIBLE);
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void refreshNotifications() {
    mNotificationManager.cancel(C2DMUtils.C2DM_NOTIFICATION_ID);
    
    if(CacheProfile.unread_likes > 0) {
      mLikesNotify.setText(" "+CacheProfile.unread_likes+" ");
      mLikesNotify.setVisibility(View.VISIBLE);
    } else
      mLikesNotify.setVisibility(View.INVISIBLE);

    if(CacheProfile.unread_messages > 0) {
      mInboxNotify.setText(" "+CacheProfile.unread_messages+" ");
      mInboxNotify.setVisibility(View.VISIBLE);
    } else
      mInboxNotify.setVisibility(View.INVISIBLE);
    
    if(CacheProfile.unread_symphaties > 0) {
      mSymphatyNotify.setText(" "+CacheProfile.unread_symphaties+" ");
      mSymphatyNotify.setVisibility(View.VISIBLE);
    } else
      mSymphatyNotify.setVisibility(View.INVISIBLE);
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
        startActivity(new Intent(this.getApplicationContext(),Likes2Activity.class));
      } break;
      case R.id.btnDshbrdSymphaty: {
        //startActivity(new Intent(this.getApplicationContext(),RatesActivity.class));
        startActivity(new Intent(this.getApplicationContext(),SymphatyActivity.class));
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
    //menu.add(0,MENU_LOG,0,"Log");
    //menu.add(0,MENU_LEAKS,0,"Leaks");
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
}
