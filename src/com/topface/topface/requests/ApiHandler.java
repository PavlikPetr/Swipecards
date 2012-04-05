package com.topface.topface.requests;

import com.topface.topface.utils.Debug;
import android.os.Handler;
import android.os.Message;

abstract public class ApiHandler extends Handler {
  @Override
  public void handleMessage(Message msg) {
    super.handleMessage(msg);
    ApiResponse response = (ApiResponse)msg.obj;
    try {
      if(response.code!=ApiResponse.RESULT_OK)
        fail(response.code,response);
      else
        success(response);
    } catch(Exception e) {
      Debug.log(this,"api handler exception");
    }
  }
  abstract public void success(ApiResponse response) throws NullPointerException;
  abstract public void fail(int codeError,ApiResponse response) throws NullPointerException;
}
