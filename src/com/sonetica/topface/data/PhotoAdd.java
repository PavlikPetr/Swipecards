package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class PhotoAdd extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static PhotoAdd parse(Response response) {
    PhotoAdd add = new PhotoAdd();
    try {
      add.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
      Debug.log("Questionary.class","Wrong response parsing: " + e);
    }
    return add;
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
