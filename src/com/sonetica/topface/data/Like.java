package com.sonetica.topface.data;

public class Like extends AbstractData {
  // Data
  public String uid;
  public String first_name;
  public String online;
  public String avatars_big;
  public String avatars_small;
  // Methods
  @Override
  public String getLink() {
    return avatars_big;
  }
}
