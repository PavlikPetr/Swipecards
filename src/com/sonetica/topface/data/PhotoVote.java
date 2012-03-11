package com.sonetica.topface.data;

import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class PhotoVote extends AbstractData {
  // Data
  public boolean completed;
  //---------------------------------------------------------------------------
  public static PhotoVote parse(Response response) {
    PhotoVote vote = new PhotoVote();
    
    try {
      vote.completed = response.mJSONResult.optBoolean("completed");
    } catch(Exception e) {
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
