package com.topface.topface.services;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.Verify;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.VerifyRequest;
import com.topface.topface.ui.dashboard.DashboardActivity;
import com.topface.topface.ui.inbox.InboxActivity;
import com.topface.topface.ui.likes.LikesActivity;
import com.topface.topface.ui.rates.RatesActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
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
  private boolean   mRunning;
  private Runnable  mLooper;
  private Messenger mMessenger;
  private Handler   mServiceHandler;
  private NotificationManager mNotificationManager;
  // Constants
  public  static final int MSG_PURCHASE = 104;
  public  static final int MSG_ACCEL    = 105;
  public  static final int MSG_DEACCEL  = 106;
  // Timer
  private static final long DEF_TIME   = 1000L * 60*60;
  private static final long ACCEL_TIME = 1000L * 60;
  private long _timer = DEF_TIME;
  // Intents
  public static final String INTENT_DATA = "data";
  public static final String INTENT_SIGNATURE = "signature";
  // Notification
  public static final int TP_MSG_NOTIFICATION   = 1001;
  public static final int TP_RATES_NOTIFICATION = 1002;
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

    mLooper = new RunTask();
    mMessenger = new Messenger(new IncomingHandler());
    mServiceHandler = new Handler();
    mServiceHandler.postDelayed(mLooper,_timer);
    mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent,int flags,int startId) {
    Debug.log("NotifyService","onStartCommand");
    mRunning = true;
    return START_STICKY;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    mRunning = false;
    
    mServiceHandler.removeCallbacks(mLooper);
    mServiceHandler.removeCallbacksAndMessages(NotificationService.class);
    mNotificationManager = null;
    mServiceHandler = null;
    mMessenger = null;
    mLooper = null;
    
    Debug.log("notifyService","onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void verifyPurchase(final String data,final String signature) {
    // сохранить ордер
    VerifyRequest verifyRequest = new VerifyRequest(getApplicationContext());
    verifyRequest.data = data;
    verifyRequest.signature = signature;
    verifyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Verify verify = Verify.parse(response);
        resources(verify.power,verify.money);
        
        // затереть ордер
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        // обратитесь в суппорт, ваш ордер
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void resources(int power,int money) {
    CacheProfile.power = power;
    CacheProfile.money = money;
  }
  //---------------------------------------------------------------------------
  private void notifacations(int messages,int likes, int rates) {
    boolean update = false;
    
    if(CacheProfile.unread_messages != messages) {
      update = true;
      CacheProfile.unread_messages = messages;
      if(_timer!=ACCEL_TIME)
        updateMessagesNotification(messages);
    }
    
    if((CacheProfile.unread_likes+CacheProfile.unread_rates) != (likes+rates)) {
      update = true;
      CacheProfile.unread_likes = likes;
      CacheProfile.unread_rates = rates;
      if(_timer!=ACCEL_TIME)
        updateRatesNotification(likes,rates);
    }
    
    if(update && _timer==ACCEL_TIME)
      broadcast(DashboardActivity.BROADCAST_ACTION);
  }
  //---------------------------------------------------------------------------
  private void updateRatesNotification(int likes,int rates) {
    if(likes+rates == 0) {
      deleteNotification(TP_RATES_NOTIFICATION);
      return;
    }
    
    int icon = R.drawable.ic_launcher;
    CharSequence text = "Вы получили новые оценки";
    long when = System.currentTimeMillis();
    Notification notification = new Notification(icon,text,when);
    String title = "Topface";
    StringBuilder body = new StringBuilder("У вас есть новые оценки");
    Intent intent = null;
    if(likes > rates)
      intent = new Intent(getApplicationContext(),LikesActivity.class);
    else
      intent = new Intent(getApplicationContext(),RatesActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);
    notification.setLatestEventInfo(getApplicationContext(),title,body,pendingIntent);
    mNotificationManager.notify(TP_RATES_NOTIFICATION, notification);
  }
  //---------------------------------------------------------------------------
  private void updateMessagesNotification(int messages) {
    if(messages == 0) {
      deleteNotification(TP_MSG_NOTIFICATION);
      return;
    }
    
    int icon = R.drawable.ic_launcher;
    CharSequence text;
    if(messages > 1)
      text = "Вы получили новые сообщения";
    else
      text = "Вы получили новое сообщение";
    
    long when = System.currentTimeMillis();
    
    Notification notification = new Notification(icon,text,when);
    String title = "Topface";
    StringBuilder body = new StringBuilder("У вас есть ");
    switch(messages) {
      case 1:
        body.append("одно новое сообщение");
        break;
      case 2:
      case 3:
      case 4:
        body.append(messages + " новых сообщения");
        break;
      default:
        body.append(messages + " новых сообщений");
        break;
    }    
    Intent intent = new Intent(getApplicationContext(),InboxActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);
    notification.setLatestEventInfo(getApplicationContext(),title,body,pendingIntent);
    mNotificationManager.notify(TP_MSG_NOTIFICATION, notification);
  }
  //---------------------------------------------------------------------------
  private void deleteNotification(int notify) {
    mNotificationManager.cancel(notify);
  }
  //---------------------------------------------------------------------------
  private void broadcast(String action) {
    Intent intent = new Intent(action);
    sendBroadcast(intent);
  }
  //---------------------------------------------------------------------------
  // class RunTask
  //---------------------------------------------------------------------------
  class RunTask implements Runnable {
    public void run() {
      if(!mRunning)
        return;
      
      if(!Http.isOnline(NotificationService.this) || App.SSID == null || App.SSID.length()==0) {
        _timer = DEF_TIME;
        mServiceHandler.postDelayed(this,_timer);
        return;
      }
      
      Debug.log("NotifyService","RunTask");
      
      ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
      profileRequest.part = ProfileRequest.P_NOTIFICATION;
      profileRequest.callback(new ApiHandler() {
        @Override
        public void success(final ApiResponse response) {
          Profile profile = Profile.parse(response);
          notifacations(profile.unread_messages,profile.unread_likes,profile.unread_rates);
        }
        @Override
        public void fail(int codeError,ApiResponse response) {
        }
      }).exec();
      
      mServiceHandler.postDelayed(this,_timer);
    }
  }
  //---------------------------------------------------------------------------
  // class IncomingHandler
  //---------------------------------------------------------------------------
  class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_DEACCEL:
          _timer = DEF_TIME;
          Debug.log("NotifyService","MSG_DEACCEL");
          mServiceHandler.postDelayed(mLooper,_timer);
          break;
        case MSG_ACCEL:
          _timer = ACCEL_TIME;
          Debug.log("NotifyService","MSG_ACCEL");
          mServiceHandler.postDelayed(mLooper,_timer);
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
}
