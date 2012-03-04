package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class PhotoVote extends AbstractData {
  // Data
  public boolean completed; // всегда TRUE
  //---------------------------------------------------------------------------
  public static PhotoVote parse(Response response) {
    PhotoVote vote = new PhotoVote();
    try {
      vote.completed = response.mJSONResult.getBoolean("completed");
    } catch(JSONException e) {
      Debug.log("PhotoVote.class","Wrong response parsing: " + e);
    }
    return vote;
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
