package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class PhotoDelete extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static PhotoDelete parse(Response response) {
    PhotoDelete delete = new PhotoDelete();
    try {
      delete.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
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
