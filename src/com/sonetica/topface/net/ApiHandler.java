package com.sonetica.topface.net;

import android.os.Handler;
import android.os.Message;

abstract public class ApiHandler extends Handler {
  @Override
  public void handleMessage(Message msg) {
    super.handleMessage(msg);
    Response response = (Response)msg.obj;
    if(response.code>0)
      fail(response.code);
    else
      success(response);
  }
  abstract public void success(Response response);
  abstract public void fail(int codeError);
}
