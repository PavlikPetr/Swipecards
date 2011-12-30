package com.sonetica.topface.net;

import com.sonetica.topface.Global;
import android.content.Context;
import android.os.Message;

public abstract class ApiRequest {
  // Data
  public  String service = "";
  public  String ssid    = "";
  private ApiHandler mHandler;
  private static String URL = "http://api.topface.ru/?v=1";
  //---------------------------------------------------------------------------
  public ApiRequest callback(ApiHandler handler) {
    mHandler = handler;
    return this;
  }
  //---------------------------------------------------------------------------
  public void exec(Context context) {
    if(mHandler==null)
      return;
    ssid = Global.SSID;
    String sResponse =  Http.httpSendTpRequest(URL,this.toString());
    Response response = new Response(sResponse);
    mHandler.sendMessage(Message.obtain(null,0,response));
  }
  //---------------------------------------------------------------------------
  @Override
  public abstract String toString();
  //---------------------------------------------------------------------------
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
  //---------------------------------------------------------------------------
}
