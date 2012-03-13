package com.sonetica.topface.ui.dashboard;

import com.sonetica.topface.App;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.ui.JLogActivity;
import com.sonetica.topface.ui.LeaksActivity;
import com.sonetica.topface.ui.PreferencesActivity;
import com.sonetica.topface.ui.dating.DatingActivity;
import com.sonetica.topface.ui.inbox.InboxActivity;
import com.sonetica.topface.ui.likes.LikesActivity;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.ui.rates.RatesActivity;
import com.sonetica.topface.ui.tops.TopsActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/*
 *  Класс главного активити для навигации по приложению  "TopFace"
 */
public class DashboardActivity extends Activity implements View.OnClickListener {
  // Data
  private boolean mBlock;
  private NotifyHandler    mNotifyHandler;
  private DashboardButton  mLikesButton;
  private DashboardButton  mRatesButton;
  private DashboardButton  mChatButton;
  private ProgressDialog   mProgressDialog;
  // Constants
  public static final int INTENT_DASHBOARD = 100;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    mLikesButton = ((DashboardButton)findViewById(R.id.btnDashbrdLikes));
    mLikesButton.setOnClickListener(this);
    mRatesButton = ((DashboardButton)findViewById(R.id.btnDashbrdRates));
    mRatesButton.setOnClickListener(this);
    mChatButton = ((DashboardButton)findViewById(R.id.btnDashbrdChat));
    mChatButton.setOnClickListener(this);
    
    ((DashboardButton)findViewById(R.id.btnDashbrdDating)).setOnClickListener(this);
    ((DashboardButton)findViewById(R.id.btnDashbrdTops)).setOnClickListener(this);
    ((DashboardButton)findViewById(R.id.btnDashbrdProfile)).setOnClickListener(this);
    
    if(!App.cached && !Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    mNotifyHandler = new NotifyHandler();
    //mNotifyHandler.sendEmptyMessage(0);
    
    update();
  }
  //---------------------------------------------------------------------------
  public void update() {
    ProfileRequest profileRequest = new ProfileRequest(this,false);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {
        Profile profile = Profile.parse(response,false);
        if(profile==null) {
          mBlock=true;
          Toast.makeText(DashboardActivity.this.getApplicationContext(),"Profile is null",Toast.LENGTH_SHORT).show();
        }
        Data.setProfile(profile);
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,Response response) {
        mBlock=true;
      }
    }).exec();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    
    System.gc();
    
    if(Data.SSID.length()>0)
      return;

    startActivity(new Intent(getApplicationContext(),SocialActivity.class));
    finish();
 }
  //---------------------------------------------------------------------------  
  @Override
  protected void onResume() {
    super.onResume();
    invalidateNotification();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {  
    if(!App.cached && !Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
    
    if(mBlock==true) {
      Toast.makeText(this,"profile is null",Toast.LENGTH_SHORT).show();
      return;
    }
    switch(view.getId()) {
      case R.id.btnDashbrdDating: {
        startActivity(new Intent(this.getApplicationContext(),DatingActivity.class));
      } break;
      case R.id.btnDashbrdLikes: {
        startActivity(new Intent(this.getApplicationContext(),LikesActivity.class));
      } break;
      case R.id.btnDashbrdRates: {
        startActivity(new Intent(this.getApplicationContext(),RatesActivity.class));
      } break;
      case R.id.btnDashbrdChat: {
        startActivity(new Intent(this.getApplicationContext(),InboxActivity.class));
      } break;
      case R.id.btnDashbrdTops: {
        startActivity(new Intent(this.getApplicationContext(),TopsActivity.class));
      } break;
      case R.id.btnDashbrdProfile: {
        startActivity(new Intent(this.getApplicationContext(),ProfileActivity.class));
      } break;      
      default:
    }
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onPause() {
    super.onPause();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mNotifyHandler = null;
    
    Data.clear();
    
    System.gc();
    
    /*
    if(DashboardButton.mRedNews!=null)
      DashboardButton.mRedNews.recycle();
    DashboardButton.mRedNews=null;
    DashboardButton.mPaint=null;
    */
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void invalidateNotification() {
    mLikesButton.mNotify = Data.s_Likes;
    mLikesButton.invalidate();
    
    mRatesButton.mNotify = Data.s_Rates;
    mRatesButton.invalidate();
    
    mChatButton.mNotify = Data.s_Messages;
    mChatButton.invalidate();    
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_ONE = 0;
  private static final int MENU_PREFERENCES = 1;
  private static final int MENU_LOG = 2;
  private static final int MENU_LEAKS = 3;
  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    menu.add(0,MENU_ONE,0,getString(R.string.dashbrd_menu_one));
    menu.add(0,MENU_PREFERENCES,0,getString(R.string.dashbrd_menu_preferences));
    menu.add(0,MENU_LOG,0,"Log");
    menu.add(0,MENU_LEAKS,0,"Leaks");
    
    return super.onCreatePanelMenu(featureId, menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ONE:
        Toast.makeText(this,getString(R.string.dashbrd_menu_one),Toast.LENGTH_SHORT).show();
        App.cached = !App.cached;
        break;
      case MENU_PREFERENCES:
        startActivity(new Intent(getApplicationContext(),PreferencesActivity.class));
        break;
      case MENU_LOG:
        startActivity(new Intent(getApplicationContext(),JLogActivity.class));
        break;
      case MENU_LEAKS:
        startActivity(new Intent(getApplicationContext(),LeaksActivity.class));
        break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
  // NotifyHandler
  //---------------------------------------------------------------------------
  class NotifyHandler extends Handler {
    private int sleep_time = 1000*30;   // ВРЕМЯ ОБНОВЛЕНИЯ НОТИФИКАЦИЙ
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      
      if(mNotifyHandler==null)
        return;

      ProfileRequest profileRequest = new ProfileRequest(DashboardActivity.this.getApplicationContext(),true);
      profileRequest.callback(new ApiHandler() {
        @Override
        public void success(final Response response) {
          DashboardActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Profile profile = Profile.parse(response,true);
              Data.updateNotification(profile);
              invalidateNotification();
              NotifyHandler.this.sendEmptyMessageDelayed(0,sleep_time);
            }
          });
        }
        @Override
        public void fail(int codeError,Response response) {
        }
      }).exec();
    }

  }
  //---------------------------------------------------------------------------
}
