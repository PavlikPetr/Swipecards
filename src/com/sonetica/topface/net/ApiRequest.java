package com.sonetica.topface.net;

import com.sonetica.topface.Data;
import com.sonetica.topface.Global;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.os.Message;

public abstract class ApiRequest {
  // Data
  public  String     ssid;
  private Context    mContext;
  private ApiHandler mHandler;
  //---------------------------------------------------------------------------
  public ApiRequest(Context context) {
    mContext = context;
    ssid = "";
  }
  //---------------------------------------------------------------------------
  public ApiRequest callback(ApiHandler handler) {
    mHandler = handler;
    return this;
  }
  //---------------------------------------------------------------------------
  public void exec() {
    if(mHandler==null) {
      Debug.log(this,"Handler not found");
      return;
    }
    ssid = Data.SSID;
    Response response = new Response(Http.httpSendTpRequest(Global.API_URL,this.toString()));
    if(response.code==3)
      reAuth();
    else
      mHandler.sendMessage(Message.obtain(null,0,response));
  }
  //---------------------------------------------------------------------------
  // перерегистрация на сервере TP
  private void reAuth() {
    Debug.log(this,"reAuth");

    final AuthToken.Token token   = new AuthToken(mContext).getToken();
    final AuthRequest authRequest = new AuthRequest(mContext);
    authRequest.platform   = token.getSocialNet();
    authRequest.sid        = token.getUserId();
    authRequest.token      = token.getTokenKey();
    authRequest.locale     = Global.LOCALE;
    authRequest.clienttype = Global.CLIENT_TYPE;
    authRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        Data.saveSSID(mContext,response.getSSID());
        ApiRequest.this.exec();
      }
      @Override
      public void fail(int codeError) {
        Debug.log(this,"Getting SSID is wrong");
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  public abstract String toString();
  //---------------------------------------------------------------------------
}
