package com.topface.topface.utils.geo;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

public abstract class GeoLocationManager extends ChangeLocationListener {

    private static final int UPDATE_TIME = 20000;// * 60 * 2;;
    private static final int UPDATE_RANGE = 0;
    private LocationManager mLocationManager;

    public GeoLocationManager(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        startLocationListener();
    }

    public Location getLastKnownLocation() {
        return mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    public boolean isGPSEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isNetworkEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private String getBestProvider() {
        if (isGPSEnabled() && isNetworkEnabled()) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
            return mLocationManager.getBestProvider(criteria, true);
        } else if (isGPSEnabled()) {
            return LocationManager.GPS_PROVIDER;
        } else if (isNetworkEnabled()) {
            return LocationManager.NETWORK_PROVIDER;
        }
        return null;
    }

    public void startLocationListener() {
        String provider = getBestProvider();
        if (!TextUtils.isEmpty(provider)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, UPDATE_RANGE, this);
        }
    }

    public void stopLocationListener() {
        mLocationManager.removeUpdates(this);
    }
}
