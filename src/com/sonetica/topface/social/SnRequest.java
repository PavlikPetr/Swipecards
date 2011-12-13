package com.sonetica.topface.social;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/*
 * Класс для создания запроса к Соц. сетям
 */
public class SnRequest {
  // Data
  String mMethodName;
  HashMap<String, Object> mRequest;
  //---------------------------------------------------------------------------
  public SnRequest(String methodName) {
    mMethodName = methodName;
    mRequest = new HashMap<String, Object>();
  }
  //---------------------------------------------------------------------------
  public SnRequest setParam(String paramName, String param) {
    mRequest.put(paramName, param);
    return this;
  }
  //---------------------------------------------------------------------------
  public SnRequest setParam(String paramName, int param) {
    mRequest.put(paramName, param);
    return this;
  }
  //---------------------------------------------------------------------------
  public SnRequest setParam(String paramName, Object param) {
    mRequest.put(paramName, param);
    return this;
  }
  //---------------------------------------------------------------------------
  public String toString() {
    Set<Entry<String,Object>> params = mRequest.entrySet();
    String requestString = mMethodName + "?";
    boolean hasEntry = false;
    for(Entry<String,Object> param : params) {
      if(hasEntry)
        requestString += "&";
      else
        hasEntry = true;

      requestString += param.getKey() + "=" + param.getValue();
    }
    return requestString;
  }
  //---------------------------------------------------------------------------
}
