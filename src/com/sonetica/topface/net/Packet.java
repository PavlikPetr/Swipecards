package com.sonetica.topface.net;

import android.os.Handler;
import android.os.Message;

public class Packet {
  // Data
  public ApiRequest  mRequest;
  public Handler  mHandler;
  // Methods
  public Packet(ApiRequest request, Handler handler) {
    mRequest = request;
    mHandler = handler;
  }
  public void sendMessage(Message message) {
    mHandler.sendMessage(message);
  }
  @Override
  public String toString() {
    return mRequest.toString();
  }
}
