package com.topface.topface.utils.geo;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;

public abstract class GeoLocationManager {

    private static final int UPDATE_TIME = 20000;// * 60 * 2;;
    private static final float UPDATE_RANGE = 0.5f;
    private LocationManager mLocationManager;
    private Location mBestLocation;

    private ChangeLocationListener mNetworkLocationListener = new ChangeLocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            compareWithBestLocation("Network", location);
        }
    };

    private ChangeLocationListener mGPSLocationListener = new ChangeLocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            compareWithBestLocation("GPS", location);
        }
    };

    public GeoLocationManager(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        startLocationListener();
    }

    public void compareWithBestLocation(String s, Location location) {
        if (location == null) {
            return;
        }
        long oldLocationTime = mBestLocation.getTime();
        long currentLOcationTime = location.getTime();
        float oldLocationAccuracy = mBestLocation.getAccuracy();
        float currentLOcationAccuracy = location.getAccuracy();
        ///"|||oldLocationTime "+oldLocationTime+"|||currentLOcationTime " + currentLOcationTime
        Debug.log("", "Location Updated " + s + "|||distance " + mBestLocation.distanceTo(location) +
                "|||oldLocationAccuracy " + oldLocationAccuracy + "|||currentLOcationAccuracy " + currentLOcationAccuracy);
    }

    public Location getLastKnownLocation() {
        mBestLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        return mBestLocation;
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

    protected abstract void onLocationChanged(Location location);

    public void startLocationListener() {
        String provider = getBestProvider();
        if (!TextUtils.isEmpty(provider)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, UPDATE_RANGE, mNetworkLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIME, UPDATE_RANGE, mGPSLocationListener);
        }
    }

    public void stopLocationListener() {
        mLocationManager.removeUpdates(mNetworkLocationListener);
        mLocationManager.removeUpdates(mGPSLocationListener);
    }
}
