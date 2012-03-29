package com.sonetica.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class CitiesRequest extends ApiRequest {
  // Data
  private String service = "cities";
  public  String type;  // тип выборки перечня городов. Пока поддерживается только “top”
  //---------------------------------------------------------------------------
  public CitiesRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("type",type));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}

