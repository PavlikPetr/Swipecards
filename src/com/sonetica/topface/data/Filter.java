package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class Filter extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static Filter parse(Response response) {
    Filter filter = new Filter();
    try {
      filter.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
      Debug.log("Filter.class","Wrong response parsing: " + e);
    }
    return filter;
  }
  //---------------------------------------------------------------------------
}
