package com.topface.topface.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Geo;
import com.topface.topface.ui.analytics.TrackedMapActivity;
import com.topface.topface.utils.GeoLocationManager;
import com.topface.topface.utils.GeoLocationManager.LocationProviderType;
import com.topface.topface.utils.OsmManager;
import com.topface.topface.utils.OsmManager.OSMAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for map displaying
 * If there is no extra objects in intent than it will locate current position of device.
 * If there are extra latitude(key:GeoMapActivity.INTENT_COORDINATES)
 * and longitude(key:GeoMapActivity.INTENT_LONGITUDE_ID) it will moveTo specific position
 *
 * @author kirussell
 */
@SuppressWarnings("deprecation")
public class GeoMapActivity extends TrackedMapActivity implements LocationListener, OnItemClickListener, OnClickListener {

    //Constants
    public static final int INTENT_REQUEST_GEO = 112;

    public static final String INTENT_GEO = "geo_data";

    private static final int DIALOG_LOCATION_PROGRESS_ID = 0;

    private static final int MAP_INITIAL_ZOOM = 18;
    private static final long LOCATION_PROVIDER_TIMEOUT = 10000;

    //Views and Adapters
    private MapView mMapView;
    private static ProgressDialog mProgressDialog;
    private AutoCompleteTextView mAddressView;
    private ArrayAdapter<String> mAddressAdapter;


    //Variables
    private GeoLocationManager mGeoLocationManager;
    private ArrayList<Address> mAddressList = new ArrayList<Address>();
    private boolean mLocationDetected = false;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ac_map);

        mGeoLocationManager = new GeoLocationManager(getApplicationContext());

        mMapView = (MapView) findViewById(R.id.mapView);

        Bundle extras = getIntent().getExtras();
        int requestKey = getIntent().getIntExtra(Static.INTENT_REQUEST_KEY, 0);

        if (requestKey == INTENT_REQUEST_GEO) {
            //init address autocompletion
            mAddressView = (AutoCompleteTextView) findViewById(R.id.mapAddress);
            mAddressAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.dropdown_item_1line, new ArrayList<String>()) {
                Filter mFilter = new AddressFilter();

                @Override
                public Filter getFilter() {
                    return mFilter;
                }
            };
            mAddressView.setAdapter(mAddressAdapter);
            mAddressView.setOnItemClickListener(this);
            mAddressView.setOnClickListener(this);

            onCurrentLocationRequest();

            findViewById(R.id.mapBtnConfirm).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mGeoLocationManager.currentPoint != null) {
                        Geo geo = new Geo(
                                mGeoLocationManager.currentAddress,
                                mGeoLocationManager.currentPoint.getLongitudeE6() / 1E6,
                                mGeoLocationManager.currentPoint.getLatitudeE6() / 1E6
                        );

                        Intent intent = GeoMapActivity.this.getIntent();
                        intent.putExtra(INTENT_GEO, geo);
                        GeoMapActivity.this.setResult(RESULT_OK, intent);
                    }

                    GeoMapActivity.this.finish();
                }
            });

            findViewById(R.id.mapBtnMyLocation).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    onCurrentLocationRequest();
                }
            });
        } else {
            findViewById(R.id.inputBar).setVisibility(View.GONE);
            GeoPoint point = ((Geo) extras.getParcelable(INTENT_GEO)).getCoordinates().getGeoPoint();

            mGeoLocationManager.setItemOverlayOnTouch(false);
            mMapView.getController().animateTo(point);
            mGeoLocationManager.setOverlayItem(getApplicationContext(), mMapView, point, MAP_INITIAL_ZOOM);
        }


    }

    private void onCurrentLocationRequest() {
        mLocationDetected = false;
        LocationProviderType providerType = mGeoLocationManager.availableLocationProvider();
        switch (providerType) {
            case GPS:
                mGeoLocationManager.setLocationListener(LocationProviderType.GPS, this);
                break;
            case AGPS:
                mGeoLocationManager.setLocationListener(LocationProviderType.AGPS, this);
                break;
            default:
                mGeoLocationManager.setLocationListener(LocationProviderType.NONE, this);
                break;
        }

        showDialog(DIALOG_LOCATION_PROGRESS_ID);

        // If GPS is to stupid, wait LOCATION_PROVIDER_TIMEOUT sec, then try AGPS)
        (new CountDownTimer(LOCATION_PROVIDER_TIMEOUT, LOCATION_PROVIDER_TIMEOUT) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                //noinspection SynchronizeOnNonFinalField
                synchronized (mGeoLocationManager) {
                    if (!mLocationDetected) {
                        mGeoLocationManager.removeLocationListener(GeoMapActivity.this);
                        if (mGeoLocationManager.isAvailable(LocationProviderType.AGPS))
                            mGeoLocationManager.setLocationListener(LocationProviderType.AGPS, GeoMapActivity.this);
                        else {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOCATION_PROGRESS_ID:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(this.getText(R.string.map_location_progress));
                return mProgressDialog;
            default:
                return super.onCreateDialog(id);
        }
    }

    @Override
    protected void onDestroy() {
        mGeoLocationManager.removeLocationListener(this);
        super.onDestroy();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    //methods for Locations
    @Override
    public void onLocationChanged(Location location) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (mGeoLocationManager) {
            GeoPoint point = GeoLocationManager.toGeoPoint(location);
            mGeoLocationManager.setOverlayItem(getApplicationContext(), mMapView, point, MAP_INITIAL_ZOOM);
            mGeoLocationManager.removeLocationListener(this);
            mLocationDetected = true;
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    //methods for address autocompletion
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 < mAddressList.size()) {
            GeoPoint point = GeoLocationManager.toGeoPoint(mAddressList.get(arg2).getLatitude(), mAddressList.get(arg2).getLongitude());
            mGeoLocationManager.setOverlayItem(getApplicationContext(), mMapView, point, MAP_INITIAL_ZOOM);
            mMapView.getController().animateTo(point);
            hideKeyboard();
        }
    }

    class AddressFilter extends Filter {

        StringBuilder mSb = new StringBuilder();

        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            ArrayList<Address> addressList = null;
            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!OsmManager.OSMSearchEnabled) {
                if (constraint != null) {
                    addressList = mGeoLocationManager.getSuggestionAddresses((String) constraint, 5);
                }
                if (addressList == null)
                    addressList = new ArrayList<Address>();

                //TODO delete empty addresses
                ArrayList<Address> emptyAddresses = new ArrayList<Address>();
                for (Address address : addressList) {
                    String addressString = createFormattedAddressFromAddress(address);
                    addressString = addressString.replace(" ", Static.EMPTY);
                    if (addressString.length() == 0) {
                        emptyAddresses.add(address);
                    }
                }

                addressList.removeAll(emptyAddresses);
            } else {
                addressList = OsmManager.getSuggestionAddresses((String) constraint, 5);
                if (addressList == null)
                    addressList = new ArrayList<Address>();
            }

            final FilterResults filterResults = new FilterResults();
            filterResults.values = addressList;
            filterResults.count = addressList.size();

            mAddressList = addressList;

            return filterResults;
        }

        private String createFormattedAddressFromAddress(final Address address) {
            if (address instanceof OSMAddress) {
                return ((OSMAddress) address).getAddress();
            } else {
                mSb.setLength(0);
                final int addressLineSize = address.getMaxAddressLineIndex();
                for (int i = 0; i < addressLineSize; i++) {
                    if (address.getAddressLine(i) != null) {
                        mSb.append(address.getAddressLine(i));
                        if (i != addressLineSize - 1) {
                            mSb.append(", ");
                        }
                    }
                }
                return mSb.toString();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(final CharSequence contraint, final FilterResults results) {
            if (mAddressAdapter != null) {
                mAddressAdapter.clear();
                if (results.values != null) {
                    for (Address address : (List<Address>) results.values) {
                        mAddressAdapter.add(createFormattedAddressFromAddress(address));
                    }
                    if (results.count > 0) {
                        mAddressAdapter.notifyDataSetChanged();
                    } else {
                        mAddressAdapter.notifyDataSetInvalidated();
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof AutoCompleteTextView) {
            ((AutoCompleteTextView) v).setText("");
        }

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAddressView.getWindowToken(), 0);
    }
}
