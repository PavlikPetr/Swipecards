package com.sonetica.topface.net;

public abstract class Request {
  // Data
  public String service = "";
  public String ssid = "";
  @Override
  public abstract String toString();
  /*
  public String toString() {
    JSONObject root = new JSONObject();
    JSONObject data = new JSONObject();
    try {
      Field[] fields = this.getClass().getFields();
      for(Field field : fields) {
        if(field.getName().equals("service") || field.getName().equals("ssid"))
          root.put(field.getName(),field.get(this));
        else
          data.put(field.getName(),field.get(this));
      }
      root.put("data",data);
    } catch(JSONException e) {} 
      catch(IllegalArgumentException e) {} 
      catch(IllegalAccessException e) {}

    return root.toString();
  }
  */
}
