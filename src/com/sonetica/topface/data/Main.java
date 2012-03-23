package com.sonetica.topface.data;

import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.utils.Debug;

public class Main extends AbstractData {
  // Data
  public boolean completed;
  //---------------------------------------------------------------------------
  public static Main parse(ApiResponse response) {
    Main main = new Main();
    
    try {
      main.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
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
