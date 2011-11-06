package com.sonetica.topface.social;

import com.sonetica.topface.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* 
 * Класс обертка над json объектом ответом из Соц. сетей
 */
public class SnResponse {
  // Data
  private JSONObject mData;
  // Constants
  public final static int RESPONSE = 0;
  public final static int ERROR = 1;
  public final static int EMPTY = 2;
  private int mResponseType = EMPTY;
  //---------------------------------------------------------------------------  
  public SnResponse(JSONObject response) throws VkResponseException, JSONException {
    if(response.has("response")) {
      JSONArray jsonArray = new JSONArray(response.getString("response"));
      mData = new JSONObject(jsonArray.getString(0));
      mResponseType = RESPONSE;
    } else if (response.has("error")) {
      mData = new JSONObject(response.getString("error"));
      mResponseType = ERROR;
    } else {
      throw new VkResponseException("Wrong response type");
    }
  }
  //---------------------------------------------------------------------------
  public Object get(String name, Object defValue) {
    if(mData.has(name)) {
      try {
        return mData.get(name);
      } catch (JSONException e) {
        Utils.log(this,"VkResponse get int error: " + e.getMessage());
        return defValue;
      }
    } else {
      return defValue;
    }
  }
  //---------------------------------------------------------------------------
  public int getInt(String name, int defValue) {
    if(mData.has(name)) {
      try {
        return mData.getInt(name);
      } catch (JSONException e) {
        Utils.log(this,"VkResponse get int error: " + e.getMessage());
        return defValue;
      }
    } else
      return defValue;
  }
  //---------------------------------------------------------------------------
  public String getString(String name, String defValue) {
    if(mData.has(name)) {
      try {
        return mData.getString(name);
      } catch (JSONException e) {
        Utils.log(this,"VkResponse get string error: " + e.getMessage());
        return defValue;
      }
    } else
      return defValue;
  }
  //---------------------------------------------------------------------------
  public int getResponseType() {
    return mResponseType;
  }
  //---------------------------------------------------------------------------
  //class VkResponseException
  //---------------------------------------------------------------------------
  static class VkResponseException extends Exception {
    public VkResponseException(String detailMessage) {
      super(detailMessage);
    }
  }//VkResponseException
  //---------------------------------------------------------------------------
}//VkResponse
