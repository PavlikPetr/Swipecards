package com.sonetica.topface.data;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;

public class Filter extends AbstractData {
  // Data
  public boolean complete; // всегда TRUE
  //---------------------------------------------------------------------------
  public static Filter parse(JSONObject response) {
    Filter filter = new Filter();
    try {
      filter.complete = response.getBoolean("complete");
    } catch(JSONException e) {
      Debug.log(null,"Wrong response parsing: " + e);
    }
    return filter;
  }
  //---------------------------------------------------------------------------
}
