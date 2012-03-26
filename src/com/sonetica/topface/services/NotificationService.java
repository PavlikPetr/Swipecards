package com.sonetica.topface.services;

import com.sonetica.topface.R;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.ui.inbox.InboxActivity;
import com.sonetica.topface.utils.Debug;
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
import android.widget.Toast;

public class NotificationService extends Service {
  // Data
  private boolean mRunning;
  private int mCounter;
  private NotificationManager mNotificationManager;
  private Handler mServiceHandler;
  private Messenger mMessenger;
  // Constants
  public  static final int MSG_BIND   = 101;
  public  static final int MSG_UNBIND = 102;
  public  static final int MSG_DELETE = 103;
  private static final long TIMER = 1000L * 6;
  private static final int TP_NOTIFICATION = 1001;
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
    mServiceHandler.postDelayed(new RunTask(),TIMER);
    mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    --mCounter;
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent,int flags,int startId) {
    mRunning = true;
    Debug.log("notifyService","onStartCommand");
    return START_STICKY; //super.onStartCommand(intent,flags,startId);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    mRunning = false;
    mServiceHandler.removeCallbacks(new RunTask());
    mServiceHandler.removeCallbacksAndMessages(NotificationService.class);
    mServiceHandler = null;
    Debug.log("notifyService","onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void createNotification(int messageNumber) {
    Debug.log("notifyService","create notify");
    int icon = R.drawable.ic_launcher;
    CharSequence tickerText = "You got new messages";
    long when = System.currentTimeMillis();
    Notification notification = new Notification(icon,tickerText,when);
    CharSequence contentTitle = "Topface";
    CharSequence contentText = "You have " + messageNumber + (messageNumber > 1 ? "new messages" : "new message");
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
          ++mCounter;
          Debug.log("notifyService","bind:" + mCounter);
          break;
        case MSG_UNBIND:
          --mCounter;
          Debug.log("notifyService","unbind:" + mCounter);
          break;
        case MSG_DELETE:
          deleteNotification();
          Debug.log("notifyService","delete:" + mCounter);
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
      Debug.log("notifyService","runtask:enter");
      // проверка на интернет
      if(mCounter == 0) {
        Debug.log("notifyService","runtask:+counter:"+mCounter);
        ProfileRequest profileRequest = new ProfileRequest(getApplicationContext(),true);
        profileRequest.callback(new ApiHandler() {
          @Override
          public void success(final ApiResponse response) {
            Debug.log("notifyService","runtask:success");
            Profile profile = Profile.parse(response,true);
            Toast.makeText(getApplicationContext(),"messages:"+profile.unread_messages,Toast.LENGTH_SHORT).show();
            if(profile.unread_messages > 0)
              createNotification(profile.unread_messages);
          }
          @Override
          public void fail(int codeError,ApiResponse response) {
            Debug.log("notifyService","runtask:fail");
          }
        }).exec();
      } else {
        Debug.log("notifyService","runtask:-counter:"+mCounter);
      }
      if(mRunning)
        mServiceHandler.postDelayed(this,TIMER);
    }
  }
  //---------------------------------------------------------------------------
}
