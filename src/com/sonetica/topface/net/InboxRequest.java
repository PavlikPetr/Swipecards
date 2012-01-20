package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class InboxRequest extends ApiRequest {
  // Data
  private String service = "feedInbox";
  public  int offset;     // смещение выбираемых сообщений
  public  int limit;      // максимальный размер выбираемых сообщений
  public  boolean isNew;  // осуществлять выборку только по новым сообщениям
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
      root.put("data",new JSONObject().put("offset",offset)
                                      .put("limit",limit)
                                      .put("new",isNew));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
