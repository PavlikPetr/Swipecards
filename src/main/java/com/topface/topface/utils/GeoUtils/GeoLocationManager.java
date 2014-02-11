package com.topface.topface.utils.GeoUtils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class GeoLocationManager {

    public static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

}
