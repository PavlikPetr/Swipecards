package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class City extends AbstractData implements SerializableToJson, Cloneable, Parcelable {
    public final static int ALL_CITIES = 0;

    public static final Parcelable.Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel source) {
            return new City(source);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

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

    public City() {
    }

    public City(JSONObject city) {
        if (city != null) {
            fillData(city);
        }
    }

    public City(int id, String name, String full) {
        this.id = id;
        this.name = name;
        this.full = full;
    }

    protected City(Parcel in) {
        id = in.readInt();
        name = in.readString();
        full = in.readString();
    }

    protected void fillData(JSONObject city) {
        this.id = city.optInt("id");
        this.name = city.optString("name");
        this.full = city.optString("full");
    }

    public static LinkedList<City> getCitiesList(IApiResponse response) {
        LinkedList<City> cities = new LinkedList<>();
        try {
            JSONArray arr = response.getJsonResult().getJSONArray("cities");
            if (arr.length() > 0)
                for (int i = 0; i < arr.length(); i++) {
                    cities.add(new City(arr.getJSONObject(i)));
                }
        } catch (Exception e) {
            Debug.error("City.class: Wrong response parsing", e);
        }

        return cities;
    }

    public static City createCity(int id, String name, String full) {
        return new City(id, name, full);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("id", id)
                .put("name", name)
                .put("full", full);
    }

    @Override
    public void fromJSON(String json) {

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        return new City(id, name, full);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof City)) return false;
        City city = ((City) o);
        return id == city.id && name.equals(city.getName()) && full.equals(city.getFullName());
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (full != null ? full.hashCode() : 0);
        return result;
    }

    public boolean isEmpty() {
        return id == 0;
    }

    public String getName() {
        if (this.id == ALL_CITIES) {
            return App.getContext().getString(R.string.filter_cities_all);
        } else {
            return name;
        }
    }

    public String getFullName() {
        return TextUtils.isEmpty(full) ? getName() : full;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(full);
    }
}
