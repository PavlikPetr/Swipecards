package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class SearchRequest extends ApiRequest {
  // Data
  private String service = "search";
  public int limit;    // размер получаемой выборки 10 <= limit <= 50
  public boolean geo;   // необходимости геопозиционного поиска
  public boolean online; // необходимость выборки только онлайн-пользователей
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
      root.put("data",new JSONObject().put("limit",limit)
                                      .put("geo",geo)
                                      .put("online",online));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
