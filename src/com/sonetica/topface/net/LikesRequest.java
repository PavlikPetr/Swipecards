package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class LikesRequest extends ApiRequest {
  // Data
  private String service = "feedLike";
  public  int offset;  // смещение выборки понравившихся
  public  int limit;   // максимальный размер выборки
  public  int from;    // идентификатор лайка, от которого делать выборку
  public  boolean only_new;  // осуществлять выборку только по новым лайкам, или по всем
  //---------------------------------------------------------------------------
  public LikesRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      JSONObject data = new JSONObject().put("limit",limit);
      if(from>0)
        data.put("from",from);
      if(only_new)
        data.put("new",only_new);
      root.put("data",data);
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
