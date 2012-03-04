package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;
import android.content.Context;

public class PhotoVoteRequest extends ApiRequest {
  // Data
  private String service = "photoVote";
  public int uid;    // идентификатор пользователя хозяина фотографии
  public int photo;  // идентификатор эротической фотографии
  public int vote;   // голос за пользователя: если больше нуля - нравится, если меньше нуля - не нравится
  //---------------------------------------------------------------------------
  public PhotoVoteRequest(Context context) {
    super(context);
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("uid",uid)
                                      .put("photo",photo)
                                      .put("vote",vote));
    } catch(JSONException e) {
      Debug.log(this,"Wrong request compiling: " + e);
    }
    return root.toString();
  }
  //---------------------------------------------------------------------------
}
