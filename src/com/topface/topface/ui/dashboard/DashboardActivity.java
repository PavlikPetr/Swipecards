package com.topface.topface.ui.dashboard;

import com.topface.topface.App;
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
import com.topface.topface.ui.likes.LikesActivity;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.rates.RatesActivity;
import com.topface.topface.ui.symphaty.SymphatyActivity;
import com.topface.topface.ui.tops.TopsActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.Imager;
import com.topface.topface.utils.LeaksManager;
import android.app.Activity;
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
  private TextView mRatesNotify;
  private NotificationReceiver mNotificationReceiver; 
  private ProgressDialog mProgressDialog;
  // Constants
  public static final String BROADCAST_ACTION = "com.topface.topface.DASHBOARD_NOTIFICATION";
  //---------------------------------------------------------------------------
  // class NotificationReceiver
  //---------------------------------------------------------------------------
  public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(BROADCAST_ACTION))
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
    
    if(!App.init && App.SSID==null || App.SSID.length()==0) {
      startActivity(new Intent(getApplicationContext(),SocialActivity.class));
      finish();
    }
    
    // notifications
    mLikesNotify = (TextView)findViewById(R.id.tvDshbrdNotifyLikes);
    mInboxNotify = (TextView)findViewById(R.id.tvDshbrdNotifyChat);
    mRatesNotify = (TextView)findViewById(R.id.tvDshbrdNotifyRates);

    // Buttons
    ((Button)findViewById(R.id.btnDshbrdDating)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdLikes)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdRates)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdChat)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdTops)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDshbrdProfile)).setOnClickListener(this);
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    // is online
    if(!Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    
    if(App.SSID==null && App.SSID.length()>0) {
      startActivity(new Intent(getApplicationContext(),SocialActivity.class));
      finish();      
    }
    
    // start broadcaster
    if(mNotificationReceiver == null) {
      mNotificationReceiver = new NotificationReceiver();
      registerReceiver(mNotificationReceiver,new IntentFilter(BROADCAST_ACTION));
    }
    
    updateProfile();
    
    System.gc();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
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
          mUpdate = true;
          CacheProfile.setData(Profile.parse(response));
          Imager.avatarOwnerPreloading(getApplicationContext());
        }
        else
          CacheProfile.updateNotifications(Profile.parse(response));
        refreshNotifications();
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        if(mProgressDialog!=null)
        mProgressDialog.cancel();
        mLikesNotify.setVisibility(View.INVISIBLE);
        mInboxNotify.setVisibility(View.INVISIBLE);
        mRatesNotify.setVisibility(View.INVISIBLE);
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void refreshNotifications() {
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
    
    if(CacheProfile.unread_rates > 0) {
      mRatesNotify.setText(" "+CacheProfile.unread_rates+" ");
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
}
