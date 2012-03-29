package com.sonetica.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class DoRateRequest extends ApiRequest {
  // Data
  private String service = "rate";
  public  int userid;   // идентификатор пользователя для оценки
  public  int rate;     // оценка пользователя. ОДЗ: 1 <= RATE <= 10
  //---------------------------------------------------------------------------
  public DoRateRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("userid",userid)
                                      .put("rate",rate));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
