package com.sonetica.topface.data;

import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.utils.Debug;

public class Message extends AbstractData {
  // Data
  public boolean completed;
  //---------------------------------------------------------------------------
  public static Message parse(ApiResponse response) {
    Message msg = new Message();
    
    try {
      msg.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
      Debug.log("Message.class","Wrong response parsing: " + e);
    }
    
    return msg;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return null;
  }
  //---------------------------------------------------------------------------
}
