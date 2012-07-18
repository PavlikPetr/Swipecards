package com.topface.topface.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
import android.widget.Button;
import android.widget.Filter;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.GeoLocationManager;
import com.topface.topface.utils.OSM;
import com.topface.topface.utils.GeoLocationManager.LocationProviderType;
import com.topface.topface.utils.OSM.OSMAddress;

/**
 * Activity for map displaying 
 * If there is no extra objects in intent than it will locate current position of device.
 * If there are extra latitude(key:GeoMapActivity.INTENT_LATITUDE_ID) 
 * and longitude(key:GeoMapActivity.INTENT_LONGITUDE_ID) it will moveTo specific position
 *   
 * @author kirussell
 *
 */
public class GeoMapActivity extends MapActivity implements LocationListener, OnItemClickListener, OnClickListener{

	//Constants
	public static final int INTENT_REQUEST_GEO = 112;
	
	public static final String INTENT_LONGITUDE_ID = "geo_longitude";
	public static final String INTENT_LATITUDE_ID = "geo_latitude";
	public static final String INTENT_ADDRESS_ID = "geo_address";
	
	private static final int DIALOG_LOCATION_PROGRESS_ID = 0;
	
	private static int MAP_INITIAL_ZOOM = 18;
	private static long LOCATION_PROVIDER_TIMEOUT = 5000;	
	
	//Views and Adapters
	private MapView mMapView;
	private ProgressDialog mProgressDialog;
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
		if (extras != null) {
			double latitude = extras.getDouble(INTENT_LATITUDE_ID);
			double longitude = extras.getDouble(INTENT_LONGITUDE_ID);
			
			mMapView.getController().animateTo(GeoLocationManager.toGeoPoint(latitude,longitude));
		} else {		
			//init address autocompletion
			mAddressView = (AutoCompleteTextView) findViewById(R.id.mapAddress);
			mAddressAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>()) {
				Filter mFilter = new AddressFilter();
				@Override
				public Filter getFilter() {
					return mFilter;
				}
			};
			mAddressView.setAdapter(mAddressAdapter);
			mAddressView.setOnItemClickListener(this);
			mAddressView.setOnClickListener(this);
//			mAddressView.setThreshold(THRESHOLD);
			
//			TextView.OnEditorActionListener listener = new TextView.OnEditorActionListener(){
//
//				@Override
//				public boolean onEditorAction(TextView arg0, int actionId,
//						KeyEvent event) {
//					if (actionId == EditorInfo.IME_ACTION_DONE && event.getAction() == KeyEvent.ACTION_DOWN) { 
//						hideKeyboard();
//					}
//					return true;
//				}
//				
//			};
//			mAddressView.setOnEditorActionListener(listener);
			
			onCurrentLocationRequest(); 
			
			((Button)findViewById(R.id.mapBtnConfirm)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mGeoLocationManager.currentPoint != null) {
						Intent intent = GeoMapActivity.this.getIntent();
						intent.putExtra(INTENT_LONGITUDE_ID, (double)(mGeoLocationManager.currentPoint.getLongitudeE6()/1E6));
						intent.putExtra(INTENT_LATITUDE_ID, (double)(mGeoLocationManager.currentPoint.getLatitudeE6()/1E6));
						intent.putExtra(INTENT_ADDRESS_ID, mGeoLocationManager.currentAddress);

						GeoMapActivity.this.setResult(RESULT_OK, intent);
					} 
					
					GeoMapActivity.this.finish();
				}
			});
			
			((Button)findViewById(R.id.mapBtnMyLocation)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onCurrentLocationRequest();
				}
			});
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
		(new CountDownTimer(LOCATION_PROVIDER_TIMEOUT,LOCATION_PROVIDER_TIMEOUT) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				
			}
			
			@Override
			public void onFinish() {
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
			mProgressDialog = new ProgressDialog(getApplicationContext());
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
		super.onDestroy();
		mProgressDialog.dismiss();
		mGeoLocationManager.removeLocationListener(this);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	//methods for Locations
	@Override
	public void onLocationChanged(Location location) {
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
			GeoPoint point = GeoLocationManager.toGeoPoint(mAddressList.get(arg2).getLatitude(),mAddressList.get(arg2).getLongitude());
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
			if (!OSM.OSMSearchEnabled) {
				if (constraint != null) {
					addressList = mGeoLocationManager.getSuggestionAddresses((String) constraint, 5);
				}
				if (addressList == null)
					addressList = new ArrayList<Address>();
				
				//TODO delete empty addresses
				ArrayList<Address> emptyAddresses = new ArrayList<Address>();
				for (Address address : addressList) {				
					String addressString  = createFormattedAddressFromAddress(address);
					addressString = addressString.replace(" ", Static.EMPTY);
					if (addressString.length() == 0) {
						emptyAddresses.add(address);
					}
				}			
				
				addressList.removeAll(emptyAddresses);
			} else {
				addressList = OSM.getSuggestionAddresses((String)constraint, 5);
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
			((AutoCompleteTextView)v).setText("");			
		}
		
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mAddressView.getWindowToken(), 0);
	}
}
