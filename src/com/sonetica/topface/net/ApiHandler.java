package com.sonetica.topface.net;

import com.sonetica.topface.utils.Debug;
import android.os.Handler;
import android.os.Message;

abstract public class ApiHandler extends Handler {
  @Override
  public void handleMessage(Message msg) {
    super.handleMessage(msg);
    Response response = (Response)msg.obj;
    try {
      if(response.code!=Response.RESULT_OK)
        fail(response.code,response);
      else
        success(response);
    } catch(Exception e) {
      Debug.log(this,"null pointer api handler");
    }
  }
  abstract public void success(Response response) throws NullPointerException;
  abstract public void fail(int codeError,Response response) throws NullPointerException;
}
