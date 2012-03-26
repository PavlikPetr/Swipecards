package com.sonetica.topface.services;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.net.ProfileRequest;
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
  public  static final int MSG_BIND   = 101;
  public  static final int MSG_UNBIND = 102;
  public  static final int MSG_DELETE = 103;
  public  static final int TP_NOTIFICATION = 1001;
  private static final long TIMER = 1000L * 10;
  //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log("notifyService","onCreate");
    
    mMessenger = new Messenger(new IncomingHandler());
    mServiceHandler = new Handler();
    mLooper = new RunTask();
    mServiceHandler.postDelayed(mLooper,TIMER);
    mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent,int flags,int startId) {
    Debug.log("notifyService","onStartCommand");
    
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
  private void createNotification(int messages,int likes, int rates) {
    Debug.log("notifyService","create notify:"+messages);
    
    Data.s_Messages = messages;
    Data.s_Likes    = likes;
    Data.s_Rates    = rates;
   
    Intent intent = new Intent(DashboardActivity.ACTION);
    sendBroadcast(intent);
    
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
          //Debug.log("notifyService","bind:" + mCounter);
          break;
        case MSG_UNBIND:
          //Debug.log("notifyService","unbind:" + mCounter);
          break;
        case MSG_DELETE:
          deleteNotification();
          //Debug.log("notifyService","delete:" + mCounter);
          break;
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
      
      // проверка на интернет и ssid
      ProfileRequest profileRequest = new ProfileRequest(getApplicationContext(),true);
      profileRequest.callback(new ApiHandler() {
        @Override
        public void success(final ApiResponse response) {
          Profile profile = Profile.parse(response,true);
          createNotification(profile.unread_messages,profile.unread_likes,profile.unread_rates);
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
