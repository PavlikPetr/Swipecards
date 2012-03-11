package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class InboxRequest extends ApiRequest {
  // Data
  private String service = "feedInbox";
  public  int offset;        // смещение выбираемых сообщений
  public  int limit;         // максимальный размер выбираемых сообщений
  public  int from;          // идентификатор сообщения, от которого делать выборку
  public  boolean only_new;  // осуществлять выборку только по новым сообщения, или по всем
  //---------------------------------------------------------------------------
  public InboxRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      JSONObject data = new JSONObject().put("limit",limit);
      if(from>0)
        data.put("from",from);
      if(only_new)
        data.put("new",only_new);
      root.put("data",data);
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
