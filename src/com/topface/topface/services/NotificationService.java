package com.topface.topface.services;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Profile;
import com.topface.topface.data.Verify;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.VerifyRequest;
import com.topface.topface.ui.dashboard.DashboardActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.widget.Toast;

public class NotificationService extends Service {
  // Data
  private long _timer;
  private boolean   mDashboard;
  private boolean   mRunning;
  private Runnable  mLooper;
  private Messenger mMessenger;
  private Handler   mServiceHandler;
  private NotificationManager mNotificationManager;
  // Constants
  public static final int MSG_PURCHASE = 104;
  public static final int MSG_ACCEL    = 105;
  public static final int MSG_DEACCEL  = 106;
  // Timer
  private static final long TIMER_DEFAULT = 1000L * 60 * 60 * 4;
  //private static final long TIMER_ACCEL   = 1000L * 60;
  // Intents
  public static final String PURCHASE_DATA = "data";
  public static final String PURCHASE_SIGNATURE = "signature";
  // Notification
  public static final int NOTIFICATION_MESSAGES = 1001;
  public static final int NOTIFICATION_LIKES    = 1002;
  // Actions
  private static final String ACTION_START_ACCEL = "com.topface.topface.START_ACCEL";
  private static final String ACTION_STOP_ACCEL  = "com.topface.topface.STOP_ACCEL";
  private static final String ACTION_PURCHASE    = "com.topface.topface.PURCHASE";
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

    _timer  = TIMER_DEFAULT;
    //mLooper = new RunTask();
    //mMessenger = new Messenger(new IncomingHandler());
    //mServiceHandler = new Handler();
    //mServiceHandler.post(mLooper);
    mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent,int flags,int startId) {
    Debug.log("NotifyService","onStartCommand");
    if(intent!=null) {
      String action = intent.getAction();
      if(action!=null) {
        if(action.equals(ACTION_START_ACCEL))
          mDashboard = true;
        else if(action.equals(ACTION_STOP_ACCEL))
          mDashboard = false;
        else if(action.equals(ACTION_PURCHASE)) {
          String data = intent.getStringExtra(PURCHASE_DATA);
          String signature = intent.getStringExtra(PURCHASE_SIGNATURE);
          verifyPurchase(data,signature);
        }
      }
    }
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
  public static void startAcceleration(Context context) {
    Intent intent = new Intent(context, NotificationService.class);
    intent.setAction(ACTION_START_ACCEL);
    context.startService(intent);
  }
  //---------------------------------------------------------------------------
  public static void stopAcceleration(Context context) {
    Intent intent = new Intent(context, NotificationService.class);
    intent.setAction(ACTION_STOP_ACCEL);
    context.startService(intent);
  }
  //---------------------------------------------------------------------------
  public static void purchase(Context context, String data, String signature) {
    Intent intent = new Intent(context, NotificationService.class);
    intent.setAction(ACTION_PURCHASE);
    intent.putExtra(PURCHASE_DATA,data);
    intent.putExtra(PURCHASE_SIGNATURE,signature);
    context.startService(intent);
  }
  //---------------------------------------------------------------------------
  private void verifyPurchase(final String data, final String signature) {
    // сохранить ордер
    final VerifyRequest verifyRequest = new VerifyRequest(getApplicationContext());
    verifyRequest.data = data;
    verifyRequest.signature = signature;
    verifyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Verify verify = Verify.parse(response);
        CacheProfile.power = verify.power;
        CacheProfile.money = verify.money;
        // затереть ордер
        broadcast(BuyingActivity.BROADCAST_PURCHASE_ACTION);
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        Toast.makeText(getApplicationContext(),getString(R.string.purchasing_error),Toast.LENGTH_LONG).show();
        // обратитесь в суппорт, ваш ордер
      }
    }).exec();
  }
//  //---------------------------------------------------------------------------
//  private void notifications(int messages,int likes, int symphaty) {
//    //boolean update = false;
//    
//    if(CacheProfile.unread_messages != messages) {
//      CacheProfile.unread_messages = messages;
//      //update = true;
//      //if(_timer!=TIMER_ACCEL)
//      //updateMessagesNotification(messages);
//    }
//    
//    if((CacheProfile.unread_likes+CacheProfile.unread_symphaties) != (likes+symphaty)) {
//      //update = true;
//      CacheProfile.unread_likes = likes;
//      CacheProfile.unread_symphaties = symphaty;
//      //if(_timer!=TIMER_ACCEL)
//      //updateLikesNotification(likes,symphaty);
//    }
//    
//    //if(update && _timer==TIMER_ACCEL)
//      //broadcast(DashboardActivity.BROADCAST_ACTION);
//  }
//  //---------------------------------------------------------------------------
//  private void updateLikesNotification(int likes,int symphaty) {
//    if(likes+symphaty == 0) {
//      deleteNotification(NOTIFICATION_LIKES);
//      return;
//    }
//    
//    int icon = R.drawable.ic_launcher;
//    CharSequence text = "Вы получили новые оценки";
//    long when = System.currentTimeMillis();
//    Notification notification = new Notification(icon,text,when);
//    String title = "Topface";
//    StringBuilder body = new StringBuilder("У вас есть новые оценки");
//    Intent intent = null;
//    /*
//    if(likes > symphaty)
//      intent = new Intent(getApplicationContext(),LikesActivity.class);
//    else
//    */
//      intent = new Intent(getApplicationContext(),DashboardActivity.class); //SymphatyActivity
//    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);
//    notification.setLatestEventInfo(getApplicationContext(),title,body,pendingIntent);
//    mNotificationManager.notify(NOTIFICATION_LIKES, notification);
//  }
  //---------------------------------------------------------------------------
  private void updateNotification(int likes, int symphaty, int messages) {
    if(likes+symphaty+messages == 0) {
      deleteNotification(NOTIFICATION_MESSAGES);
      return;
    }
    
    int icon = R.drawable.ic_statusbar;
    
    CharSequence text;
    if(messages > 1)
      text = getString(R.string.notification_title_messages);
    else if(messages == 1)
      text = getString(R.string.notification_title_message);
    else
      text = getString(R.string.notification_title_rates);
    
    long when = System.currentTimeMillis();
    
    Notification notification = new Notification(icon,text,when);
    String title = "Topface";
    StringBuilder body = new StringBuilder(getString(R.string.notification_have)+" ");
    switch(messages) {
      case 0:
        break;
      case 1:
        body.append(getString(R.string.notification_message));
        break;
      case 2:
      case 3:
      case 4:
        body.append(messages + " " +  getString(R.string.notification_messages));
        break;
      default:
        body.append(messages + " " +  getString(R.string.notification_messages1));
        break;
    }
    if(symphaty+likes > 0) {
      if(messages > 0)
        body.append(" " + getString(R.string.notification_rates));
      else
        body.append(" " + getString(R.string.notification_rates1));
    }
    Intent intent = new Intent(getApplicationContext(),DashboardActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);
    notification.setLatestEventInfo(getApplicationContext(),title,body,pendingIntent);
    mNotificationManager.notify(NOTIFICATION_MESSAGES, notification);
  }
  //---------------------------------------------------------------------------
  private void deleteNotification(int notify) {
    mNotificationManager.cancelAll();
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
      
      if(!mRunning) return;
      
      if(Http.isOnline(getApplicationContext()) && App.SSID != null && App.SSID.length()>0) {
        Debug.log("NotifyService","RunTask");
        if(!mDashboard) {
          ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
          profileRequest.part = ProfileRequest.P_NOTIFICATION;
          profileRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
              Profile profile = Profile.parse(response);
              updateNotification(profile.unread_likes,profile.unread_symphaties,profile.unread_messages);
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
              //
            }
          }).exec();
        }
      }
      
      mServiceHandler.postDelayed(this,_timer);
    }
  }
  //---------------------------------------------------------------------------
  // class IncomingHandler
  //---------------------------------------------------------------------------
//  class IncomingHandler extends Handler {
//    @Override
//    public void handleMessage(Message msg) {
//      switch (msg.what) {
//        case MSG_DEACCEL:
//          _timer = DEF_TIME;
//          Debug.log("NotifyService","MSG_DEACCEL");
//          mServiceHandler.postDelayed(mLooper,_timer);
//          break;
//        case MSG_ACCEL:
//          _timer = ACCEL_TIME;
//          Debug.log("NotifyService","MSG_ACCEL");
//          mServiceHandler.postDelayed(mLooper,_timer);
//          break;
//        case MSG_PURCHASE: {
//          Bundle bundle = msg.getData();
//          String data = bundle.getString(INTENT_DATA);
//          String signature = bundle.getString(INTENT_SIGNATURE);
//          verifyPurchase(data,signature);
//        } break;
//        default:
//          super.handleMessage(msg);
//      }
//    }
//  }
  //---------------------------------------------------------------------------
}
