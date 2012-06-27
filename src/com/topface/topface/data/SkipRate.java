package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SkipRate extends AbstractData {
  // Data
  public boolean completed;
  //---------------------------------------------------------------------------
  public static SkipRate parse(ApiResponse response) {
    SkipRate skip = new SkipRate();
    
    try {
      skip.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
      Debug.log("SkipRate.class","Wrong response parsing: " + e);
    }
    
    return skip;
  }
  //---------------------------------------------------------------------------
}
