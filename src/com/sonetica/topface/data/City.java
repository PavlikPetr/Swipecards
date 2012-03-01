package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class City extends AbstractData {
  // Data
  public int id;      // уникальных код города
  public String name; // строка наименования города в русскоязычной локали
  public String full; // строка наименования города в русскоязычной локали
  //---------------------------------------------------------------------------
  public static LinkedList<City> parse(Response response) {
    LinkedList<City> cities = new LinkedList<City>();
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("cities");
      if(arr.length()>0)
        for(int i=0;i<arr.length();i++) {
          JSONObject item = arr.getJSONObject(i);
          City city = new City();
            city.id   = item.getInt("id");
            city.name = item.getString("name");
            city.full = item.getString("full");
          cities.add(city);
        }
    } catch(JSONException e) {
      Debug.log("City.class","Wrong response parsing: " + e);
    }
    return cities;
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
