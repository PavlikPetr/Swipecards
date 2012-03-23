package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class VerifyRequest extends ApiRequest {
  // Data
  private String service = "verify";
  public String  data;       // строка данных заказа от Google Play
  public String  signature;  // подпись данных заказа
  //---------------------------------------------------------------------------
  public VerifyRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      JSONObject jsondata = new JSONObject().put("data",data).put("signature",signature);
      root.put("data",jsondata);
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
