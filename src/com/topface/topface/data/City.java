package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class City extends AbstractData {
    /**
     * уникальных код города
     */
    public int id;
    /**
     * название города в локали пользователя
     */
    public String name;
    /**
     * полное название города + область + страна
     */
    public String full;

    public City(JSONObject city) {
        super(city);
    }

    protected void fillData(JSONObject city) {
        this.id = city.optInt("id");
        this.name = city.optString("name");
        this.full = city.optString("full");
    }

    public static LinkedList<City> parse(ApiResponse response) {
        LinkedList<City> cities = new LinkedList<City>();

        try {
            JSONArray arr = response.jsonResult.getJSONArray("cities");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    cities.add(new City(arr.getJSONObject(i)));
                }
        } catch (Exception e) {
            Debug.log("City.class", "Wrong response parsing: " + e);
        }

        return cities;
    }
}
