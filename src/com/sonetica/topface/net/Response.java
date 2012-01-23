package com.sonetica.topface.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sonetica.topface.utils.Debug;

public class Response {
  // Data
  public int code;
  public JSONObject mJSONResult;
  // Constants
  public static final int FATAL_ERROR = 99;
  //---------------------------------------------------------------------------
  public JSONObject getSearch() {
    return mJSONResult;
  }
  //---------------------------------------------------------------------------
  public Response(String response) {
    if(response==null) {
      Debug.log(this,"json response is null");
      code = FATAL_ERROR;
      return;
    }
    try {
      mJSONResult = new JSONObject(response);
      if(!mJSONResult.isNull("error")) {
        mJSONResult = mJSONResult.getJSONObject("error");
        code = mJSONResult.getInt("code");
      } else if(!mJSONResult.isNull("result")) {
        mJSONResult = mJSONResult.getJSONObject("result");
      } else {
        code = FATAL_ERROR;
      }
    } catch (JSONException e) {
      code = FATAL_ERROR;
      Debug.log(this,"json resonse is wrong:" + response);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public String toString() {
    if(mJSONResult!=null)
      return mJSONResult.toString();
    else
      return "response is null";
  }
  //---------------------------------------------------------------------------
}
