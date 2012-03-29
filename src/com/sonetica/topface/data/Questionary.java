package com.sonetica.topface.data;

import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.utils.Debug;

public class Questionary extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static Questionary parse(ApiResponse response) {
    Questionary questionary = new Questionary();
    
    try {
      questionary.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
      Debug.log("Questionary.class","Wrong response parsing: " + e);
    }
    
    return questionary;
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
