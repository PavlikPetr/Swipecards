package com.sonetica.topface.net;

import com.sonetica.topface.Global;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.os.Message;

public abstract class ApiRequest {
  // Data
  public  String service = "";
  public  String ssid = "";
  private Context mContext;
  private ApiHandler mHandler;
  private static String URL = "http://api.topface.ru/?v=1";
  //---------------------------------------------------------------------------
  public ApiRequest(Context context) {
    mContext = context;
  }
  //---------------------------------------------------------------------------
  public ApiRequest callback(ApiHandler handler) {
    mHandler = handler;
    return this;
  }
  //---------------------------------------------------------------------------
  public void exec() {
    if(mHandler==null)
      return;
    ssid = Global.SSID;
    Response response = new Response(Http.httpSendTpRequest(URL,this.toString()));
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
    authRequest.platform = token.getSocialNet();
    authRequest.sid      = token.getUserId();
    authRequest.token    = token.getTokenKey();
    authRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        Global.saveSSID(mContext,response.getSSID());
        ApiRequest.this.exec();
      }
      @Override
      public void fail(int codeError) {
        // ???
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  public abstract String toString();
  //---------------------------------------------------------------------------
}

/*
public String toString() {
  JSONObject root = new JSONObject();
  JSONObject data = new JSONObject();
  try {
    Field[] fields = this.getClass().getFields();
    for(Field field : fields) {
      if(field.getName().equals("service") || field.getName().equals("ssid"))
        root.put(field.getName(),field.get(this));
      else
        data.put(field.getName(),field.get(this));
    }
    root.put("data",data);
  } catch(JSONException e) {} 
    catch(IllegalArgumentException e) {} 
    catch(IllegalAccessException e) {}

  return root.toString();
}
*/
