package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class RatesRequest extends ApiRequest {
  // Data
  private String service = "feedRates";
  public  int offset;  // смещение выбираемых оценок
  public  int limit;   // максимальный размер выборки
  //---------------------------------------------------------------------------
  public RatesRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("offset",offset).put("limit",limit));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
