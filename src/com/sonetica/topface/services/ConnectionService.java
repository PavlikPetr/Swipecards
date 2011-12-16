package com.sonetica.topface.services;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import com.sonetica.topface.App;
import com.sonetica.topface.net.AuthRequest;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.Packet;
import com.sonetica.topface.net.Request;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.utils.Debug;

public class ConnectionService extends Service {
  // Data
  private static String URL  = "http://api.topface.ru/?v=1";
  private static ServiceHandler serviceHandler;
  //---------------------------------------------------------------------------
  // class ServiceHandler
  //---------------------------------------------------------------------------
  private final class ServiceHandler extends Handler {
    public ServiceHandler(Looper looper) {
      super(looper);
      Debug.log(this,"+started");
    }
    @Override
    public void handleMessage(Message msg) {
      try {
        Packet packet  = (Packet)msg.obj;
        String sResponse = Http.httpSendTpRequest(URL,packet.toString());
        Response resp = new Response(sResponse);
        if(resp.code==3)
          sendAuth(packet);
        else
          packet.sendMessage(Message.obtain(null,0,resp));
        Debug.log(null,"resp:"+sResponse);
      } catch (Exception e) {
        Debug.log(this,"packet is wrong");
      }
    }
  }//ServiceHandler
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    Debug.log(this,"+onCreate");
    
    HandlerThread thread = new HandlerThread("ServiceStartArguments",Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    
    serviceHandler = new ServiceHandler(thread.getLooper());
  }
  //---------------------------------------------------------------------------
  private void sendAuth(final Packet packet) {
    AuthToken.Token token = new AuthToken(this).getToken();
    AuthRequest authRequest = new AuthRequest();
    authRequest.platform = token.getSocialNet();
    authRequest.sid      = token.getUserId();
    authRequest.token    = token.getTokenKey();
    sendRequest(authRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        if(resp.code==-1) {
          App.saveSSID(ConnectionService.this,resp.getSsid());
          try {
            String sResponse = Http.httpSendTpRequest(URL,packet.toString());
            packet.sendMessage(Message.obtain(null,0,new Response(sResponse)));
          } catch(Exception e) { }
        } else {
          //FAIL
        }
      }
    }); 
  }
  //---------------------------------------------------------------------------
  public static void sendRequest(Request request, Handler handler) {
    request.ssid = App.SSID;
    serviceHandler.sendMessage(Message.obtain(null,0,new Packet(request,handler)));
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    serviceHandler = null;
    Debug.log(this,"-onDestroy");
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

