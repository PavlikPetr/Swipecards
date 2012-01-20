package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class FilterRequest extends ApiRequest {
  // Data
  private String service = "filter";
  public  int sex;      // код пола пользователей для поиска
  public  int city ;    // идентификатор города для поиска пользователей
  public  int agebegin; // начальный возраст пользователей в выборке поиска
  public  int ageend;   // конечный возраст пользователей в выборке поиска
  // Enum
  public static final int FEMALE = 0;
  public static final int MALE   = 1;
  //---------------------------------------------------------------------------
  public FilterRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("sex",sex)
                                      .put("city",city)
                                      .put("agebegin",agebegin)
                                      .put("ageend",ageend));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
