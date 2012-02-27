package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Main extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static Main parse(Response response) {
    Main main = new Main();
    try {
      main.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
      Debug.log("Main.class","Wrong response parsing: " + e);
    }
    return main;
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
