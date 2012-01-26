package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Message extends AbstractData {
  // Data
  public boolean completed;  // результат отправления сообщения, всегда TRUE
  //---------------------------------------------------------------------------
  public static Message parse(Response response) {
    Message msg = new Message();
    try {
      msg.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
      Debug.log("Message.class","Wrong response parsing: " + e);
    }
    return msg;
  }
  //---------------------------------------------------------------------------
}
