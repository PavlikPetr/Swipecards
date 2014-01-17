package com.topface.topface.utils.GeoUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.view.LayoutInflater;

import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.OsmManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GeoLocationManager:
 * - sets location update listener
 * - check providers availability
 * - take MapView cache (Bitmap)
 * - request address by location latitude and longitude
 *
 * @author kirussell
 */
public class GeoLocationManager {

    private static int mPinHeight = 0;

    public static enum LocationProviderType {GPS, AGPS, NONE}

    public String currentAddress;

    private LocationManager mLocationManager;
    private Geocoder mGeocoder;

    private LayoutInflater mInflater;

    private static Drawable mPinDrawable;

    private boolean mTouchable = true;

    public GeoLocationManager(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mGeocoder = new Geocoder(context, Locale.getDefault());
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPinDrawable = context.getResources().getDrawable(R.drawable.map_pin);
        mPinHeight = mPinDrawable.getMinimumHeight();
    }

    /**
     * Type of available Location Provider
     *
     * @return available type of location provider (priority: GPS->AGPS)
     */
    public LocationProviderType availableLocationProvider() {
        boolean gpsEnabled = false;
        boolean networkEnabled;

        //GPS
        try {
            gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gpsEnabled) {
                return LocationProviderType.GPS;
            }
        } catch (Exception ex) {
            Debug.log(this, ex.toString());
        }

        if (!gpsEnabled) {
            //AGPS
            try {
                networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (networkEnabled) {
                    return LocationProviderType.AGPS;
                }
            } catch (Exception ex) {
                Debug.log(this, ex.toString());
            }
        }

        return LocationProviderType.NONE;
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public boolean isAvailable(LocationProviderType type) {
        String internalType;
        switch (type) {
            case GPS:
                internalType = LocationManager.GPS_PROVIDER;
                break;
            case AGPS:
            default:
                internalType = LocationManager.NETWORK_PROVIDER;
                break;
        }

        return mLocationManager.isProviderEnabled(internalType);

    }

    /**
     * Sets the Location Listener for listening update changes
     *
     * @param type     provider type (GPS,AGPS)
     * @param listener object which implements the LocationListener interface
     */
    public void setLocationListener(LocationProviderType type, LocationListener listener) {
        switch (type) {
            case GPS:
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
                break;
            case AGPS:
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
                break;
            default:
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, listener);
                break;
        }
    }

    public void removeLocationListener(LocationListener listener) {
        mLocationManager.removeUpdates(listener);
    }

    /**
     * Address by coordinates
     *
     * @param latitude  широта
     * @param longitude долго
     * @return specific address correlating with input coordinates
     */
    public String getLocationAddress(double latitude, double longitude) {
        if (OsmManager.OSMReverseEnabled) {
            currentAddress = OsmManager.getAddress(latitude, longitude);
            return currentAddress;
        } else {
            StringBuilder sBShortLocation = new StringBuilder();
            StringBuilder sBFullLocation = new StringBuilder();
            try {
                List<Address> listAddresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    final String street = listAddresses.get(0).getAddressLine(0);
                    final String city = listAddresses.get(0).getAddressLine(1);
                    final String country = listAddresses.get(0).getAddressLine(2);
                    sBShortLocation.append(street);
                    if (city != null) sBShortLocation.append(",\n").append(city);

                    sBFullLocation.append(street);
                    if (city != null) sBFullLocation.append(", ").append(city);
                    if (country != null) sBFullLocation.append(", ").append(country);
                }
                currentAddress = sBFullLocation.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sBShortLocation.toString();
        }

    }


    /**
     * Addresses that correlate with user input
     *
     * @return addresses' suggestions
     */
    public ArrayList<Address> getSuggestionAddresses(String text, int maxResultsNumber) {
        ArrayList<Address> result = new ArrayList<Address>();
        try {
            result.addAll(mGeocoder.getFromLocationName(text, maxResultsNumber));
        } catch (IOException ex) {
            Debug.log(this, "Failed to get autocomplete suggestions \n" + ex.toString());
        }

        return result;
    }


    public Location getLastKnownLocation() {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        return mLocationManager.getLastKnownLocation(locationProvider);
    }

}
