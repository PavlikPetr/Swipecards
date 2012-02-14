package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class MessageRequest extends ApiRequest {
  // Data
  private String service = "message";
  public int userid;      // идентификатор пользователя, кому послали сообщение
  public String message;  // текст сообщения в UTF-8. min размер текста - 1 символ, max - 1024 
  //---------------------------------------------------------------------------
  public MessageRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("userid",userid).put("message",message));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    return root.toString();
  }
  //---------------------------------------------------------------------------
}

