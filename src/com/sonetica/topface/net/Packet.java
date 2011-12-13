package com.sonetica.topface.net;

import android.os.Handler;
import android.os.Message;

public class Packet {
  // Data
  public Request  mRequest;
  public Handler  mHandler;
  // Methods
  public Packet(Request request, Handler handler) {
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
