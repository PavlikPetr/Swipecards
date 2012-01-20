package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sonetica.topface.utils.Debug;

public class Response {
  // Data
  public int code;
  JSONObject mJsonResult;
  // Constants
  public static final int FATAL_ERROR = 99;
  //---------------------------------------------------------------------------
  public JSONObject getSearch() {
    return mJsonResult;
  }
  //---------------------------------------------------------------------------
  public Response(String response) {
    if(response==null) {
      Debug.log(this,"json response is null");
      code = FATAL_ERROR;
      return;
    }
    JSONObject obj = null;
    try {
      obj = new JSONObject(response);
      if(!obj.isNull("error")) {
        mJsonResult = obj.getJSONObject("error");
        code = mJsonResult.getInt("code");
      } else if(!obj.isNull("result"))
        mJsonResult = obj.getJSONObject("result");
      else
        code = FATAL_ERROR;
    } catch (JSONException e) {
      code = FATAL_ERROR;
      Debug.log(this,"json resonse is wrong:" + response);
    }
  }
  //---------------------------------------------------------------------------
}
