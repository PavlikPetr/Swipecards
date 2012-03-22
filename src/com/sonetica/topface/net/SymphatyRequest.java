package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class SymphatyRequest extends ApiRequest {
  // Data
  private String service = "feedSymphaty";
  /*
  * {Number} limit - максимальный размер выборки входящих симпатий
  {Number} from - начальный идентификатор симпатии для выборки
  {Boolean} new - осуществлять выборку только по непрочитанным симпатиям
  */
  //---------------------------------------------------------------------------
  public SymphatyRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      //root.put("data",new JSONObject().put("sex",sex).put("city",city));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
