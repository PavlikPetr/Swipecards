package com.sonetica.topface.net;

import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.data.Auth;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.os.Message;

public abstract class ApiRequest {
  // Data
  public String ssid;  // volatile
  private Context mContext;
  private ApiHandler mHandler;
  //private static ExecutorService mThreadsPool; // что за хуйня с пулом потоков ?
  //---------------------------------------------------------------------------
  public ApiRequest(Context context) {
    ssid = "";
    mContext = context;
    //mThreadsPool = Executors.newSingleThreadExecutor();
  }
  //---------------------------------------------------------------------------
  public ApiRequest callback(ApiHandler handler) {
    mHandler = handler;
    return this;
  }
  //---------------------------------------------------------------------------
  public void exec() {
    ssid = Data.SSID;
    new Thread() {
      @Override
      public void run() {
        String rawResponse = Http.httpSendTpRequest(Global.API_URL,ApiRequest.this.toString());
        if(rawResponse==null)
          rawResponse = Http.httpSendTpRequest(Global.API_URL,ApiRequest.this.toString());
        Response response = new Response(rawResponse);
        if(response.code == Response.SESSION_NOT_FOUND) // ошибка авторизации
          reAuth();  // реавторизация на сервере топфейса
        else 
          mHandler.sendMessage(Message.obtain(null,0,response));
    }}.start();
  }
  //---------------------------------------------------------------------------
  // перерегистрация на сервере TP
  private void reAuth() {
    Debug.log(this,"reAuth");

    final AuthToken.Token token   = new AuthToken(mContext).getToken();
    final AuthRequest authRequest = new AuthRequest(mContext);
    authRequest.platform      = token.getSocialNet();
    authRequest.sid           = token.getUserId();
    authRequest.token         = token.getTokenKey();
    authRequest.locale        = Global.LOCALE;
    authRequest.clienttype    = Global.CLIENT_TYPE;
    authRequest.clientversion = Global.CLIENT_VERSION;
    authRequest.clientdevice  = Global.CLIENT_DEVICE;
    authRequest.clientid      = Global.CLIENT_ID;

    Response response = new Response(Http.httpSendTpRequest(Global.API_URL,authRequest.toString()));
    if(response.code == Response.RESULT_OK) {
      Auth auth = Auth.parse(response);
      Data.saveSSID(mContext,auth.ssid);
      ssid = Data.SSID;
      
      response = new Response(Http.httpSendTpRequest(Global.API_URL,ApiRequest.this.toString()));
      
      mHandler.sendMessage(Message.obtain(null,0,response));
    } else
      mHandler.sendMessage(Message.obtain(null,0,response));
  }
  //---------------------------------------------------------------------------
  public static void shutdown() {
    //mThreadsPool.shutdown();
  }
  //---------------------------------------------------------------------------
}

/*  работа с пулом потоков
if(mHandler != null)
  mThreadsPool.execute(new Runnable() {
    @Override
    public void run() {
      Looper.prepare();
      
      ssid = Data.SSID;
      
      Response response = new Response(Http.httpSendTpRequest(Global.API_URL,ApiRequest.this.toString()));
      if(response.code == 3)
        reAuth();
      else
        mHandler.sendMessage(Message.obtain(null,0,response));
      
      Looper.loop();
    }
  });
else
  Debug.log(this,"Handler not found");
  */

/*  работа с пулом потоков
authRequest.callback(new ApiHandler() {
  @Override
  public void success(Response response) {
    Auth auth = Auth.parse(response);
    Data.saveSSID(mContext,auth.ssid);
    ApiRequest.this.exec();
  }
  @Override
  public void fail(int codeError) {
    Debug.log(this,"Getting SSID is wrong");
  }
}).exec();
*/

/*
class LooperThread extends Thread {
  public Handler mHandler;
  public void run() {
    Looper.prepare();
    mHandler = new Handler() {
      public void handleMessage(Message msg) {
        // process incoming messages here
      }
    };
    Looper.loop();
  }
}
*/