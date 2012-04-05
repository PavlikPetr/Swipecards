package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Settings extends AbstractData {
  // Data
  public boolean completed;
  //---------------------------------------------------------------------------
  public static Settings parse(ApiResponse response) {
    Settings settings = new Settings();
    
    try {
      settings.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
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
