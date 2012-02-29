package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Questionary extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static Questionary parse(Response response) {
    Questionary questionary = new Questionary();
    try {
      questionary.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
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
