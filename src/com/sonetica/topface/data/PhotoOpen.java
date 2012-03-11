package com.sonetica.topface.data;

import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class PhotoOpen extends AbstractData {
  // Data
  public int money;   // количество монет текущего пользователя
  public boolean completed;
  //---------------------------------------------------------------------------
  public static PhotoOpen parse(Response response) {
    PhotoOpen open = new PhotoOpen();
    
    try {
      open.completed = response.mJSONResult.optBoolean("completed");
      open.money     = response.mJSONResult.optInt("money");
    } catch(Exception e) {
      Debug.log("PhotoOpen.class","Wrong response parsing: " + e);
    }
    
    return open;
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
