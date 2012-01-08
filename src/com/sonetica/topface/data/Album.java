package com.sonetica.topface.data;

public class Album extends AbstractData {
  // Data
  public String id;
  public String small;
  public String big;
  // Methods
  @Override
  public String getLink() {
    return big;
  }
}
