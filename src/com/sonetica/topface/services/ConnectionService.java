package com.sonetica.topface.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.Packet;
import com.sonetica.topface.net.Request;
import com.sonetica.topface.net.Response;

public class ConnectionService extends Service {
  // Data
  public  static String SSID = "";
  private static String URL  = "http://api.topface.ru/?v=1";
  private static ServiceHandler serviceHandler;
  //---------------------------------------------------------------------------
  // class ServiceHandler
  //---------------------------------------------------------------------------
  private final class ServiceHandler extends Handler {
    public ServiceHandler(Looper looper) {
      super(looper);
    }
    @Override
    public void handleMessage(Message msg) {
      try {
        Packet packet  = (Packet)msg.obj;
        String sResponse = Http.httpSendTpRequest(URL,packet.toString());
        packet.sendMessage(Message.obtain(null,0,new Response(sResponse)));
      } catch (Exception e) {}
    }
  }//ServiceHandler
  //---------------------------------------------------------------------------
  public static void sendRequest(Request request, Handler handler) {
    request.ssid = SSID;
    serviceHandler.sendMessage(Message.obtain(null,0,new Packet(request,handler)));
  }
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    HandlerThread thread = new HandlerThread("ServiceStartArguments",Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    SharedPreferences preferences = this.getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    SSID = preferences.getString(getString(R.string.ssid),"");
    
    serviceHandler = new ServiceHandler(thread.getLooper());
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
  }
  //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  //---------------------------------------------------------------------------
//  private void showNotification(String msg) {
//    Intent intent = new Intent(this,DashboardActivity.class);
//    NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//    Notification notification = new Notification(android.R.drawable.ic_notification_overlay,"Notify", System.currentTimeMillis());
//    notification.setLatestEventInfo(this,"App Name","Description of the notification",PendingIntent.getActivity(this.getBaseContext(), 0, intent,PendingIntent.FLAG_CANCEL_CURRENT));
//    mManager.notify(1001, notification);
//  }
  //---------------------------------------------------------------------------
}//ConnectionService

