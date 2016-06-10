package com.topface.topface.utils.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.state.TopfaceAppState;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * При запуске экрана "Ближайших" проверяем какие из провайдеров включены у пользователя.
 * На каждый из обнаруженных регистрируем лисентер в PeopleNearbyFragment. Отписываем  лисентеры
 * там же, если выходим из экрана ближайших или находясь в этом экране выключаем ВСЮ навигацию
 */
public class GeoLocationManager {

    @Inject
    TopfaceAppState mAppState;
    private static final int UPDATE_TIME = 1000 * 60 * 2;
    private static final float UPDATE_RANGE = 10f;
    private LocationManager mLocationManager;
    private Location mBestLocation;
    private ChangeLocationListener mNetworkLocationListener = new ChangeLocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Debug.log("GeoLocationManager Receive location from Network");
            if (isValidLocation(location)) {
                compareWithBestLocation(location);
            } else {
                Debug.log("GeoLocationManager Location not valid lat " + location.getLatitude() + " lang " + location.getLongitude());
            }
        }
    };
    private ChangeLocationListener mGPSLocationListener = new ChangeLocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Debug.log(this, "GeoLocationManager Receive location from GPS");
            if (isValidLocation(location)) {
                compareWithBestLocation(location);
            } else {
                Debug.log("GeoLocationManager Location not valid lat " + location.getLatitude() + " lang " + location.getLongitude());
            }
        }
    };

    private BroadcastReceiver mGeoStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getEnabledProvider() != GeoLocationManager.NavigationType.DISABLE) {
                startLocationListener();
            } else {
                stopLocationListener();
            }
        }
    };

    public GeoLocationManager() {
        App.get().inject(this);
        mLocationManager = (LocationManager) App.getContext().getSystemService(Context.LOCATION_SERVICE);
        startLocationListener();
    }

    @Nullable
    public static Location getCurrentLocation() {
        LocationManager locationManager = (LocationManager) App.getContext().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnown = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        return isValidLocation(lastKnown) ? lastKnown : null;
    }

    @Nullable
    public Location getLastKnownLocation() {
        Location lastKnown = mLocationManager.getLastKnownLocation(NETWORK_PROVIDER);
        setBestLocation(isValidLocation(lastKnown) ? lastKnown : null);
        return mBestLocation;
    }

    private boolean isGPSEnabled() {
        return mLocationManager.isProviderEnabled(GPS_PROVIDER);
    }

    private boolean isNetworkEnabled() {
        return mLocationManager.isProviderEnabled(NETWORK_PROVIDER);
    }

    public NavigationType getEnabledProvider() {
        if (isGPSEnabled() && isNetworkEnabled()) {
            return NavigationType.ALL;
        } else if (isNetworkEnabled()) {
            return NavigationType.NETWORK_ONLY;
        } else if (isGPSEnabled()) {
            return NavigationType.GPS_ONLY;
        } else {
            return NavigationType.DISABLE;
        }
    }

    public void startLocationListener() {
        Debug.log(this, "GeoLocationManager attach listeners");
        switch (getEnabledProvider()) {
            case ALL:
            case GPS_ONLY:
                mLocationManager.requestLocationUpdates(GPS_PROVIDER, UPDATE_TIME, UPDATE_RANGE, mGPSLocationListener);
                if (getEnabledProvider() != NavigationType.ALL) {
                    break;
                }
            case NETWORK_ONLY:
                mLocationManager.requestLocationUpdates(NETWORK_PROVIDER, UPDATE_TIME, UPDATE_RANGE, mNetworkLocationListener);
                break;
        }

    }

    public void stopLocationListener() {
        Debug.log(this, "GeoLocationManager remove listeners");
        mLocationManager.removeUpdates(mNetworkLocationListener);
        mLocationManager.removeUpdates(mGPSLocationListener);
    }


    private void compareWithBestLocation(Location location) {
        if (location == null) {
            return;
        }
        if (mBestLocation == null) {
            setBestLocation(location);
            return;
        }
        //GPS точку принимаем только в том случае если ее точность больше чем у Network
        if (location.getProvider().equals(GPS_PROVIDER) &&
                mBestLocation.getAccuracy() > location.getAccuracy()) {
            setBestLocation(location);
        }
    }

    private void setBestLocation(@Nullable Location location) {
        if (isValidLocation(location)) {
            mBestLocation = location;
            mAppState.setData(mBestLocation);
        }
    }

    public enum NavigationType {GPS_ONLY, NETWORK_ONLY, ALL, DISABLE}

    /**
     * Пустая реализация интерфейсa. Избавляемся от неиспользуемых методов.
     */
    private abstract class ChangeLocationListener implements LocationListener {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Debug.log("GeoLocationManager Enabled " + provider);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Debug.log("GeoLocationManager Disabled " + provider);
        }
    }

    public void registerProvidersChangedActionReceiver() {
        App.getContext().registerReceiver(mGeoStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    public void unregisterProvidersChangedActionReceiver() {
        App.getContext().unregisterReceiver(mGeoStateReceiver);
    }

    public static boolean isValidLocation(@Nullable Location location) {
        if (location == null) {
            return false;
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        return latitude <= 90 && latitude >= -90 && longitude <= 180 && longitude >= -180;
    }

}
