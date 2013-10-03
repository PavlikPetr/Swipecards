package com.topface.topface.utils.GeoUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import com.topface.topface.Static;

public class GeoPreferencesManager {
    public static String LATITUDE = "latitude";
    public static String LONGITUDE = "longitude";

    SharedPreferences mPreferences;
    Context context;

    public GeoPreferencesManager(Context context) {
        this.context = context;
        mPreferences = context.getSharedPreferences(Static.PREFERENCES_TAG_GEO, Context.MODE_PRIVATE);
    }

    public Location loadLastLocation() {
        final Location location = new Location(LocationManager.NETWORK_PROVIDER);
        double latitude = mPreferences.getFloat(LATITUDE, 1000);
        double longitude = mPreferences.getFloat(LONGITUDE, 1000);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        if(location.getLatitude() == 1000 || location.getLongitude() == 1000) {
            return null;
        }
        return location;
    }

    public void saveLocation(final Location location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (location != null) {
                    mPreferences.edit().putFloat(LATITUDE, (float) location.getLatitude()).putFloat(LONGITUDE, (float) location.getLongitude()).commit();
                }
            }
        }).start();
    }
}
