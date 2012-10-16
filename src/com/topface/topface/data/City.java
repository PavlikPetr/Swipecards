package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class City extends AbstractData {
    // Data
    public int id; // уникальных код города
    public String name; // строка наименования города в русскоязычной локали
    public String full; // строка наименования города в русскоязычной локали

    //---------------------------------------------------------------------------
    public static LinkedList<City> parse(ApiResponse response) {
        LinkedList<City> cities = new LinkedList<City>();

        try {
            JSONArray arr = response.mJSONResult.getJSONArray("cities");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    cities.add(parseCity(arr.getJSONObject(i)));
                }
        } catch (Exception e) {
            Debug.log("City.class", "Wrong response parsing: " + e);
        }

        return cities;
    }

    public static City parseCity(JSONObject item) {
        City city = new City();
        if (item != null) {
            city.id = item.optInt("id");
            city.name = item.optString("name");
            city.full = item.optString("full");
        }
        return city;

    }
}
