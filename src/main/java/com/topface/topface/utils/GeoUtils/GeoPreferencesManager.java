package com.topface.topface.utils.GeoUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.config.AppConfig;

public class GeoPreferencesManager {
    AppConfig mAppConfig;

    Context context;

    public GeoPreferencesManager(Context context) {
        this.context = context;
        mAppConfig = App.getAppConfig();
    }

    @SuppressWarnings("UnusedDeclaration")
    public Location loadLastLocation() {
        final Location location = new Location(LocationManager.NETWORK_PROVIDER);
        double latitude = mAppConfig.getDeviceLattitude();
        double longitude = mAppConfig.getDeviceLongitude();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        if (location.getLatitude() == 1000 || location.getLongitude() == 1000) {
            return null;
        }
        return location;
    }

    public void saveLocation(final Location location) {
        mAppConfig.setDeviceLattitude(location.getLatitude());
        mAppConfig.setDeviceLongitude(location.getLongitude());
        mAppConfig.saveConfig();
    }
}
