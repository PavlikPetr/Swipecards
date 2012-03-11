package com.sonetica.topface.net;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class ProfilesRequest extends ApiRequest {
  // Data
  private String service = "profiles";
  public  ArrayList<Integer> uids   = new ArrayList<Integer>(); // массив id пользователя в топфейсе
  public  ArrayList<String>  fields = new ArrayList<String>();  // массив интересующих полей профиля
  //---------------------------------------------------------------------------
  public ProfilesRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("uids",new JSONArray(uids)));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
