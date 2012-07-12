package com.topface.topface.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.topface.topface.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

/**
 * GeoLocationManager: 
 * - sets location update listener
 * - check providers availability
 * - take MapView cache (Bitmap)
 * - request address by location latitude and longitude
 * @author kirussell
 *
 */
public class GeoLocationManager {

	public static enum LocationProviderType {GPS,AGPS,NONE}; 
	
	public GeoPoint currentPoint;
	
	private LocationManager mLocationManager;	
	private Geocoder mGeocoder;
	
	public GeoLocationManager(Context context) {
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);	
		mGeocoder = new Geocoder(context, Locale.getDefault());
	}
	
	/**
	 * Type of available Location Provider
	 * @return available type of location provider (priority: GPS->AGPS)
	 */
	public LocationProviderType availableLocationProvider() {
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        //GPS
        try {
            gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gpsEnabled) {
                return LocationProviderType.GPS;
            }
        } catch(Exception ex) {
        	Debug.log(this, ex.toString());
        }
        
        if (!gpsEnabled) {
	        //AGPS
	        try {
	            networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	            if (networkEnabled) {
	                return LocationProviderType.AGPS;
	            }	            
	        } catch(Exception ex) {
	        	Debug.log(this, ex.toString());
	        }
        }
        
		return LocationProviderType.NONE;
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
 		
 		if (mLocationManager.isProviderEnabled(internalType)) 
 			return true;
 		
 		return false;
 	}
	
	/**
	 * Sets the Location Listener for listening update changes 
	 * @param type provider type (GPS,AGPS)
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
	 * @param latitude
	 * @param longitude
	 * @return specific address correlating with input coordinates
	 */
	public String getLocationAddress(double latitude, double longitude) {
		String location = "";
		try {
		    List<Address> listAddresses = mGeocoder.getFromLocation(latitude, longitude, 1);
		    if(null != listAddresses && listAddresses.size() > 0){
		        location = listAddresses.get(0).getAddressLine(0);
		        location += ", " + listAddresses.get(0).getAddressLine(1);
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}		
		
		return location;
	}
	
	/**
	 * Address by coordinates
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public String getLocationAddress(GeoPoint point) {
		return getLocationAddress(point.getLatitudeE6()/1E6, point.getLongitudeE6()/1E6);
	}
	
	/**
	 * Addresses that correlate with user input
	 * @param text
	 * @param maxResultsNumber
	 * @return addresses' suggestions
	 */
	public List<Address> getSuggestionAddresses(String text, int maxResultsNumber) {
		List<Address> result = new LinkedList<Address>();
		try {
			result = mGeocoder.getFromLocationName(text, maxResultsNumber);
        } catch (IOException ex) {
        	Debug.log(this, "Failed to get autocomplete suggestions \n" + ex.toString());
        }
		
		return result;
	}
	
	//-------------Static methods---------------
	
	/**
	 * Set icon on specific location and shift to that location
	 * @param context
	 * @param mapView
	 * @param point
	 * @param zoom
	 */
	public void setOverlayItem(Context context, MapView mapView, GeoPoint point, int zoom) {
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = context.getResources().getDrawable(R.drawable.ic_launcher);
		GeoItemizedOverlay itemizedoverlay = new GeoItemizedOverlay(drawable, context);
		
		String address = getLocationAddress(point);
		OverlayItem overlayitem = new OverlayItem(point, "", address);
		
		mapOverlays.clear();
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);		
		shiftToPoint(mapView, point, zoom);
		currentPoint = point;
	}
	
	/**
	 * Read MapView cache
	 * @param mapView view with map
	 * @param point specific coordinates
	 * @param zoom map zoom
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
		return new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
	}
	
	public static GeoPoint toGeoPoint(double latitude,double longitude) {
		return new GeoPoint((int)(latitude*1E6), (int)(longitude*1E6));
	}
	
	class GeoItemizedOverlay extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		Context mContext;

		public GeoItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.clear();
			mOverlays.add(overlay);
			populate();
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
		
		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {			
			String address = GeoLocationManager.this.getLocationAddress(p.getLatitudeE6()/1E6, p.getLongitudeE6()/1E6);
			currentPoint = p;
			addOverlay(new OverlayItem(p, "", address));
			return super.onTap(p, mapView);
		}
		
		@Override
	    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow)
	    {
	        super.draw(canvas, mapView, shadow);

	        if (shadow == false)
	        {
	            //cycle through all overlays
	            for (int index = 0; index < mOverlays.size(); index++)
	            {
	                OverlayItem item = mOverlays.get(index);

	                // Converts lat/lng-Point to coordinates on the screen
	                GeoPoint point = item.getPoint();
	                Point ptScreenCoord = new Point() ;
	                mapView.getProjection().toPixels(point, ptScreenCoord);

	                //Paint
	                Paint paint = new Paint();
	                paint.setTextAlign(Paint.Align.CENTER);
	                paint.setTypeface(Typeface.DEFAULT_BOLD);
	                paint.setTextSize(16);
	                paint.setARGB(150, 0, 0, 0); // alpha, r, g, b (Black, semi see-through)

	                //show text to the right of the icon
	                canvas.drawText(item.getSnippet(), ptScreenCoord.x, ptScreenCoord.y+16, paint);
	            }
	        }
	    }


	}
}
