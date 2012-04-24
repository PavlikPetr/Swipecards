package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

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
}
