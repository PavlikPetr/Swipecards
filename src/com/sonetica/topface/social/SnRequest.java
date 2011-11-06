package com.sonetica.topface.social;

import java.util.HashMap;
import java.util.Map;
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
    Set params = mRequest.entrySet();
    String requestString = mMethodName + "?";
    boolean hasEntry = false;
    for(Object param : params) {
      if(hasEntry)
        requestString += "&";
      else
        hasEntry = true;
      Map.Entry entry = (Map.Entry) param;
      requestString += entry.getKey() + "=" + entry.getValue();
    }
    return requestString;
  }
  //---------------------------------------------------------------------------
}
