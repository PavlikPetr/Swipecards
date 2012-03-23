package com.sonetica.topface.services;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.data.Auth;
import com.sonetica.topface.net.AuthRequest;
import com.sonetica.topface.net.Packet;
import com.sonetica.topface.net.ApiRequest;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;

public class ConnectionService extends Service {
  // Data
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
      Packet packet = (Packet)msg.obj;
      ApiResponse response = new ApiResponse(sendPacket(packet));
      if(response.code==3)
        reAuth(packet);
      else
        packet.sendMessage(Message.obtain(null,0,response));
    }
  }//ServiceHandler
  //---------------------------------------------------------------------------
  // формирование запроса внутри приложения к коннект сервису
  public static void sendRequest(ApiRequest request, Handler handler) {
    request.ssid = Data.SSID;
    serviceHandler.sendMessage(Message.obtain(null,0,new Packet(request,handler)));
  }
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    Debug.log(this,"+onCreate");
    HandlerThread thread = new HandlerThread("ServiceStartArguments",Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    serviceHandler = new ServiceHandler(thread.getLooper());
  }
  //---------------------------------------------------------------------------
  // перерегистрация на сервере TP
  private void reAuth(final Packet packet) {
    Debug.log(this,"service reAuth");

    AuthToken.Token token   = new AuthToken(getApplicationContext()).getToken();
    AuthRequest authRequest = new AuthRequest(getApplicationContext());
    authRequest.platform = token.getSocialNet();
    authRequest.sid      = token.getUserId();
    authRequest.token    = token.getTokenKey();
    
    sendRequest(authRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        ApiResponse ssidResponse = (ApiResponse)msg.obj;
        if(ssidResponse.code==0) {
          Auth auth = Auth.parse(ssidResponse);
          Data.saveSSID(ConnectionService.this.getApplicationContext(),auth.ssid);
          packet.mRequest.ssid = auth.ssid;
          ApiResponse response = new ApiResponse(sendPacket(packet));
          packet.sendMessage(Message.obtain(null,0,response));
        } else if(ssidResponse.code>0) {
            ; // ?????????
        } else
          ConnectionService.this.startActivity(new Intent(ConnectionService.this.getApplicationContext(),SocialActivity.class));
      }
    });
  }
  //---------------------------------------------------------------------------
  // отправка пакета на сервер TP
  private String sendPacket(Packet packet) {
    String sResponse = null;
    sResponse =  Http.httpSendTpRequest(Global.API_URL,packet.toString());
    Debug.log(this,"resp:" + sResponse);
    return sResponse;
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

