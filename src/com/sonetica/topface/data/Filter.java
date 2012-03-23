package com.sonetica.topface.data;

import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.utils.Debug;

public class Filter extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static Filter parse(ApiResponse response) {
    Filter filter = new Filter();
    
    try {
      filter.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
      Debug.log("Filter.class","Wrong response parsing: " + e);
    }
    
    return filter;
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
