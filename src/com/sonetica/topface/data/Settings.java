package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Settings extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static Settings parse(Response response) {
    Settings settings = new Settings();
    try {
      settings.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
      Debug.log("Settings.class","Wrong response parsing: " + e);
    }
    return settings;
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
