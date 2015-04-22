package com.topface.topface.utils.geo;

import android.location.LocationListener;
import android.os.Bundle;

import com.topface.framework.utils.Debug;

/**
 * Created by onikitin on 21.04.15.
 * Пустая реализация интерфейся. Избавляемся от неиспользуемых методов.
 */
public abstract class ChangeLocationListener implements LocationListener {

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Debug.log(GeoLocationManager.class, "Enabled " + provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Debug.log(GeoLocationManager.class, "Disabled " + provider);
    }
}
