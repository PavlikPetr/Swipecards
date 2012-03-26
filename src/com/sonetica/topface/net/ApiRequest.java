package com.sonetica.topface.net;

import com.sonetica.topface.App;
import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.data.Auth;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.LeaksManager;
import android.content.Context;
import android.os.Message;

public abstract class ApiRequest {
  // Data
  public String ssid;  // volatile
  private Context mContext;
  private ApiHandler mHandler;
  private static int LOOP = 5;
  //---------------------------------------------------------------------------
  public ApiRequest(Context context) {
    ssid = "";
    mContext = context;
  }
  //---------------------------------------------------------------------------
  public ApiRequest callback(ApiHandler handler) {
    mHandler = handler;
    return this;
  }
  //---------------------------------------------------------------------------
  public void exec() {
    ssid = Data.SSID;
    Thread t = new Thread("api request") {
      @Override
      public void run() {
        String rawResponse = null;
        
        int counter = 0;
        
        do {
          rawResponse = Http.httpSendTpRequest(Global.API_URL,ApiRequest.this.toString());
          
          if(rawResponse==null)
            Debug.log(App.TAG,"loop:"+counter);

          if(counter == LOOP) 
            break;
          else 
            counter++;
        } while(rawResponse == null);
        
        ApiResponse response = new ApiResponse(rawResponse);
        
        if(response.code == ApiResponse.SESSION_NOT_FOUND) // ошибка авторизации
          reAuth();  // реавторизация на сервере топфейса
        else 
          mHandler.sendMessage(Message.obtain(null,0,response));
    }};
    LeaksManager.getInstance().monitorObject(t);
    t.start();
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

    ApiResponse response = new ApiResponse(Http.httpSendTpRequest(Global.API_URL,authRequest.toString()));
    if(response.code == ApiResponse.RESULT_OK) {
      Auth auth = Auth.parse(response);
      Data.saveSSID(mContext,auth.ssid);
      ssid = Data.SSID;
      
      response = new ApiResponse(Http.httpSendTpRequest(Global.API_URL,ApiRequest.this.toString()));
      
      mHandler.sendMessage(Message.obtain(null,0,response));
    } else
      mHandler.sendMessage(Message.obtain(null,0,response));
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