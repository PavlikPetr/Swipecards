package com.topface.topface.requests;

import org.json.JSONObject;

import com.topface.topface.utils.Debug;

public class ApiResponse {
  // Data
  public int code = -1;
  public JSONObject mJSONResult;
  // Constants
  public static final int RESULT_OK = -1;
  public static final int SYSTEM = 0;
  public static final int UNKNOWN_SOCIAL_USER = 1;
  public static final int UNKNOWN_PLATFORM = 2;
  public static final int SESSION_NOT_FOUND = 3;
  public static final int UNSUPPORTED_CITIES_FILTER = 4;
  public static final int MISSING_REQUIRE_PARAMETER = 5;
  public static final int USER_NOT_FOUND = 6;
  public static final int UNSUPPORTED_LOCALE = 7;
  public static final int CANNOT_SENT_RATE = 8;
  public static final int MESSAGE_TOO_SHORT = 9;
  public static final int CANNOT_SENT_MESSAGE = 10;
  public static final int DETECT_FLOOD = 11;
  public static final int INCORRECT_PHOTO_URL = 12;
  public static final int DEFAULT_ERO_PHOTO = 13;
  public static final int PAYMENT = 14;
  public static final int INCORRECT_VOTE = 15;
  public static final int INVALID_TRANSACTION = 16;
  public static final int INVALID_PRODUCT = 17;
  public static final int INVERIFIED_RECEIPT = 18;
  public static final int ITUNES_CONNECTION = 19;
  public static final int INVERIFIED_TOKEN = 20;
  // local
  public static final int NULL_RESPONSE  = 100;
  public static final int WRONG_RESPONSE = 101;
  //---------------------------------------------------------------------------
  public JSONObject getSearch() {
    return mJSONResult;
  }
  //---------------------------------------------------------------------------
  public ApiResponse(String response) {
    try {
      if(response == null) {
        Debug.log(this,"json response is null");
        code = NULL_RESPONSE;
        return;
      }
      
      mJSONResult = new JSONObject(response);
      if(!mJSONResult.isNull("error")) {
        mJSONResult = mJSONResult.getJSONObject("error");
        code = mJSONResult.getInt("code");
      } else if(!mJSONResult.isNull("result"))
        mJSONResult = mJSONResult.getJSONObject("result");
      else
        code = WRONG_RESPONSE;
    } catch (Exception e) {
      code = WRONG_RESPONSE;
      Debug.log(this,"json resonse is wrong:" + response);
    }
  }
  //---------------------------------------------------------------------------
  public ApiResponse(JSONObject response) {
    try {
      if(response == null) {
        Debug.log(this,"json response is null");
        code = NULL_RESPONSE;
        return;
      }
      
      mJSONResult = response;
      if(!mJSONResult.isNull("error")) {
        mJSONResult = mJSONResult.getJSONObject("error");
        code = mJSONResult.getInt("code");
      } else if(!mJSONResult.isNull("result"))
        mJSONResult = mJSONResult.getJSONObject("result");
      else
        code = WRONG_RESPONSE;
    } catch (Exception e) {
      code = WRONG_RESPONSE;
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
