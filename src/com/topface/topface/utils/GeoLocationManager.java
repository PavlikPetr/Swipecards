package com.topface.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.maps.*;
import com.topface.topface.R;
import com.topface.topface.Static;

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

    public GeoPoint currentPoint;
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

    public boolean availableLocationProvider(LocationProviderType type) {
        String provider;

        switch (type) {
            case GPS:
                provider = LocationManager.GPS_PROVIDER;
                break;
            case AGPS:
                provider = LocationManager.NETWORK_PROVIDER;
                break;
            default:
                provider = LocationManager.PASSIVE_PROVIDER;
                break;
        }

        return mLocationManager.isProviderEnabled(provider);
    }

    public boolean isAvailable(LocationProviderType type) {
        String internalType = LocationManager.PASSIVE_PROVIDER;
        switch (type) {
            case GPS:
                internalType = LocationManager.GPS_PROVIDER;
                break;
            case AGPS:
                internalType = LocationManager.NETWORK_PROVIDER;
                break;
            default:
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
     * @param latitude
     * @param longitude
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
     * Address by coordinates
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public String getLocationAddress(GeoPoint point) {
        return getLocationAddress(point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6);
    }

    /**
     * Addresses that correlate with user input
     *
     * @param text
     * @param maxResultsNumber
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

    //-------------Static methods---------------

    /**
     * Set icon on specific location and shift to that location
     *
     * @param context
     * @param mapView
     * @param point
     * @param zoom
     */
    public void setOverlayItem(Context context, MapView mapView, GeoPoint point, int zoom) {
        List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.clear();
//		mPinHeight = mPinDrawable.getIntrinsicHeight();
        GeoItemizedOverlay itemizedOverlay = new GeoItemizedOverlay(mPinDrawable, context);

        String address = getLocationAddress(point);
        OverlayItem overlayitem = new OverlayItem(point, "", address);

        shiftToPoint(mapView, point, zoom);
        itemizedOverlay.addOverlay(overlayitem, mapView);
        mapOverlays.add(itemizedOverlay);
    }

    /**
     * Read MapView cache
     *
     * @param mapView view with map
     * @param point   specific coordinates
     * @param zoom    map zoom
     * @return bitmap of caches map
     */
    public static Bitmap getMapImage(MapView mapView, GeoPoint point, int zoom) {
        shiftToPoint(mapView, point, zoom);

        mapView.setDrawingCacheEnabled(true);
        Bitmap bmp = Bitmap.createBitmap(mapView.getDrawingCache());
        mapView.setDrawingCacheEnabled(false);

        return bmp;
    }

    public static void shiftToPoint(MapView mapView, GeoPoint point, int zoom) {
        mapView.getController().animateTo(point);
        if (zoom > mapView.getMaxZoomLevel())
            mapView.getController().setZoom(mapView.getMaxZoomLevel());
        else
            mapView.getController().setZoom(zoom);
    }

    public static GeoPoint toGeoPoint(Location location) {
        return new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
    }

    public static GeoPoint toGeoPoint(double latitude, double longitude) {
        return new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
    }

    public void setItemOverlayOnTouch(boolean touchable) {
        mTouchable = touchable;
    }

    class GeoItemizedOverlay extends ItemizedOverlay<OverlayItem> {
        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

        Context mContext;
        View mAddressView;

        public GeoItemizedOverlay(Drawable defaultMarker, Context context) {
            super(boundCenterBottom(defaultMarker));
            mContext = context;
            mAddressView = mInflater.inflate(R.layout.item_map_address, null);
        }

        @Override
        protected OverlayItem createItem(int i) {
            return mOverlays.get(i);
        }

        public void addOverlay(OverlayItem overlay, final MapView mapView) {
            mOverlays.clear();
            mOverlays.add(overlay);
            populate();

            final GeoPoint point = overlay.getPoint();

            mapView.removeAllViewsInLayout();
            mapView.addView(mAddressView, getTipLayout(mapView, point));

            final TextView tvAddress = (TextView) mAddressView.findViewById(R.id.map_address);
            tvAddress.setText(Static.EMPTY);
            final ProgressBar progressBar = (ProgressBar) mAddressView.findViewById(R.id.prsMapAddressLoading);
            progressBar.setVisibility(View.VISIBLE);
            (new Thread() {
                public void run() {
                    final String address = GeoLocationManager.this.getLocationAddress(point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6);

                    tvAddress.post(new Runnable() {

                        @Override
                        public void run() {
                            tvAddress.setText(address);
                            progressBar.setVisibility(View.INVISIBLE);
                            mapView.invalidate();
                        }
                    });


                }
            }).start();
            currentPoint = point;
        }

        private MapView.LayoutParams getTipLayout(MapView mapView, GeoPoint point) {
            Point screenPoint = new Point();
            mapView.getProjection().toPixels(point, screenPoint);
            GeoPoint p = mapView.getProjection().fromPixels(screenPoint.x, screenPoint.y - mPinHeight);
            return new MapView.LayoutParams(
                    MapView.LayoutParams.WRAP_CONTENT,
                    MapView.LayoutParams.WRAP_CONTENT,
                    p,
                    MapView.LayoutParams.BOTTOM_CENTER
            );
        }

        @Override
        public int size() {
            return mOverlays.size();
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {
            //String address = GeoLocationManager.this.getLocationAddress(p.getLatitudeE6()/1E6, p.getLongitudeE6()/1E6);
            if (mTouchable) {
                currentPoint = p;
                addOverlay(new OverlayItem(p, "", ""), mapView);

                return true;
            } else {
                return false;
            }
        }

        @Override
        public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, false);
            if (!shadow) {
                mAddressView.setLayoutParams(getTipLayout(mapView, currentPoint));
            }
        }
    }

}
