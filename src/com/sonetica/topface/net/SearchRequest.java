package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class SearchRequest extends ApiRequest {
  // Data
  private String service = "search";
  public  String limit;  // размер получаемой выборки 10 <= limit <= 50  
  //---------------------------------------------------------------------------
  public SearchRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("limit",limit));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
