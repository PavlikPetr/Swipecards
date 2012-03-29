package com.sonetica.topface.data;

import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.utils.Debug;

public class PhotoDelete extends AbstractData {
  // Data
  public boolean completed;
  //---------------------------------------------------------------------------
  public static PhotoDelete parse(ApiResponse response) {
    PhotoDelete delete = new PhotoDelete();
    
    try {
      delete.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
      Debug.log("PhotoDelete.class","Wrong response parsing: " + e);
    }
    
    return delete;
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
