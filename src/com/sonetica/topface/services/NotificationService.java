package com.sonetica.topface.services;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.data.Verify;
import com.sonetica.topface.requests.ApiHandler;
import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.requests.ProfileRequest;
import com.sonetica.topface.requests.VerifyRequest;
import com.sonetica.topface.ui.dashboard.DashboardActivity;
import com.sonetica.topface.ui.inbox.InboxActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class NotificationService extends Service {
  // Data
  //private int mCounter;
  private boolean mRunning;
  private Messenger mMessenger;
  private Handler mServiceHandler;
  private NotificationManager mNotificationManager;
  private Runnable mLooper;
  // Constants
  public  static final int MSG_BIND     = 101;
  public  static final int MSG_UNBIND   = 102;
  public  static final int MSG_DELETE   = 103;
  public  static final int MSG_PURCHASE = 104;
  public  static final String INTENT_DATA = "data";
  public  static final String INTENT_SIGNATURE = "signature";
  public  static final int TP_NOTIFICATION = 1001;
  private static final long TIMER = 1000L * 60*60;
  //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log("NotifyService","onCreate");
    
    mMessenger = new Messenger(new IncomingHandler());
    mServiceHandler = new Handler();
    mLooper = new RunTask();
    mServiceHandler.postDelayed(mLooper,TIMER);
    mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent,int flags,int startId) {
    Debug.log("NotifyService","onStartCommand");
    mRunning = true;
    return START_STICKY; //super.onStartCommand(intent,flags,startId);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    mRunning = false;
    mServiceHandler.removeCallbacks(mLooper);
    mServiceHandler.removeCallbacksAndMessages(NotificationService.class);
    mServiceHandler = null;
    mMessenger = null;
    mLooper = null;
    
    Debug.log("notifyService","onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void broadcast(String action) {
    Intent intent = new Intent(action);
    sendBroadcast(intent);
  }
  //---------------------------------------------------------------------------
  private void notifacations(int messages,int likes, int rates) {
    Data.s_Messages = messages;
    Data.s_Likes    = likes;
    Data.s_Rates    = rates;
  }
  //---------------------------------------------------------------------------
  private void resources(int power,int money) {
    Data.s_Power = power;
    Data.s_Money = money;
  }
  //---------------------------------------------------------------------------
  private void verifyPurchase(final String data,final String signature) {
    VerifyRequest verifyRequest = new VerifyRequest(getApplicationContext());
    verifyRequest.data = data;
    verifyRequest.signature = signature;
    verifyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Verify verify = Verify.parse(response);
        resources(verify.power,verify.money);
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void updateNotification(int messages) {
    int icon = R.drawable.ic_launcher;
    CharSequence tickerText;
    if(messages > 1)
      tickerText = "Вы получили новые сообщения";
    else
      tickerText = "Вы получили новое сообщение";
    long when = System.currentTimeMillis();
    Notification notification = new Notification(icon,tickerText,when);
    String contentTitle = "Topface";
    StringBuilder contentText = new StringBuilder("У вас есть ");
    switch(messages) {
      case 0:
        deleteNotification();
        return;
      case 1:
        contentText.append("одно новое сообщение");
        break;
      case 2:
      case 3:
      case 4:
        contentText.append(messages + " новых сообщения");
        break;
      default:
        contentText.append(messages + " новых сообщений");
        break;
    }    
    Intent notificationIntent = new Intent(getApplicationContext(),InboxActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,0);
    notification.setLatestEventInfo(getApplicationContext(),contentTitle,contentText,contentIntent);
    mNotificationManager.notify(TP_NOTIFICATION, notification);
  }
  //---------------------------------------------------------------------------
  private void deleteNotification() {
    mNotificationManager.cancel(TP_NOTIFICATION);
  }
  //---------------------------------------------------------------------------
  // class IncomingHandler
  //---------------------------------------------------------------------------
  class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_BIND:
          break;
        case MSG_UNBIND:
          break;
        case MSG_DELETE:
          deleteNotification();
          break;
        case MSG_PURCHASE: {
          Bundle bundle = msg.getData();
          String data = bundle.getString(INTENT_DATA);
          String signature = bundle.getString(INTENT_SIGNATURE);
          verifyPurchase(data,signature);
        } break;
        default:
          super.handleMessage(msg);
      }
    }
  }
  //---------------------------------------------------------------------------
  // class RunTask
  //---------------------------------------------------------------------------
  class RunTask implements Runnable {
    public void run() {
      if(!mRunning)
        return;
      
      if(!Http.isOnline(NotificationService.this) || Data.SSID == null || Data.SSID.length()==0) {
        mServiceHandler.postDelayed(this,TIMER);
        return;
      }
      
      ProfileRequest profileRequest = new ProfileRequest(getApplicationContext(),true);
      profileRequest.callback(new ApiHandler() {
        @Override
        public void success(final ApiResponse response) {
          Profile profile = Profile.parse(response,true);
          notifacations(profile.unread_messages,profile.unread_likes,profile.unread_rates);
        }
        @Override
        public void fail(int codeError,ApiResponse response) {
        }
      }).exec();
      
      mServiceHandler.postDelayed(this,TIMER);
    }
  }
  //---------------------------------------------------------------------------
}
