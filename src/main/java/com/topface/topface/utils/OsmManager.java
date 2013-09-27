package com.topface.topface.utils;

import android.location.Address;
import android.os.Handler;
import android.os.Message;

import com.topface.topface.Static;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class OsmManager {

    public static final boolean OSMSearchEnabled = false;
    public static final boolean OSMReverseEnabled = true;

    public static final String OSM_URL = "http://nominatim.openstreetmap.org";
    public static final String OSM_SEARCH_SUB = "search";
    public static final String OSM_REVERSE_SUB = "reverse";
    public static final String OSM_QUERY = "q";
    public static final String OSM_RESULT_FORMAT = "format";
    public static final String OSM_LATITUDE = "lat";
    public static final String OSM_LONGITUDE = "lon";
    public static final String OSM_ZOOM = "zoom";
    public static final String OSM_DETALIZATION = "addressdetails";
    public static final String OSM_POLYGON = "polygon";
    public static final String OSM_DISPLAY_NAME = "display_name";
    public static final String OSM_LOCALE = "accept-language";


    public static String resultFormat = "json";
    public static final String zoom = "18";
    public static final String detalization = "1";
    public static final String polygon = "0";

    public static String getAddress(double lat, double lon) {
        StringBuilder resultSB = new StringBuilder();

        try {
            JSONObject responseJSON = new JSONObject(HttpUtils.httpGetRequest(getAddressRequest(lat, lon)));
            //result = responseJSON.getString(OSM_DISPLAY_NAME);
            JSONObject details = responseJSON.getJSONObject("address");
            resultSB.append(details.optString("road"));

            String str = details.optString("house_number");
            if (str.length() > 0) {
                resultSB.append(", ").append(str);
            }

            str = details.optString("state");
            if (str.length() > 0) {
                resultSB.append(", ").append(str);
            }

            str = details.optString("country");
            if (str.length() > 0) {
                resultSB.append(", ").append(str);
            }

        } catch (Exception e) {
            Debug.error(e);
        }
        return resultSB.toString();
    }

    public static void getAddress(final double lat, final double lon, final Handler handler) {
        (new Thread() {
            @Override
            public void run() {
                String address = getAddress(lat, lon);
                Message msg = new Message();
                msg.obj = address;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public static ArrayList<Address> getSuggestionAddresses(String text, int maxNumber) {
        ArrayList<Address> result = new ArrayList<Address>();

        String query = text.replace(" ", "%20");

        try {
            JSONArray responseJSON = new JSONArray(HttpUtils.httpGetRequest(getSearchRequest(query)));
            for (int i = 0; i < responseJSON.length() || i < maxNumber; i++) {
                JSONObject item = responseJSON.getJSONObject(i);
                String address = item.getString(OSM_DISPLAY_NAME);
                double lat = item.getDouble(OSM_LATITUDE);
                double lon = item.getDouble(OSM_LONGITUDE);
                result.add(new OSMAddress(Locale.getDefault(), address, lat, lon));
            }
        } catch (JSONException e) {
            Debug.log("OSM", e.toString());
        }

        return result;
    }

    private static String getAddressRequest(double lat, double lon) {
        StringBuilder sB = new StringBuilder();
        sB.append(OSM_URL).append(Static.SLASH).append(OSM_REVERSE_SUB).append(Static.QUESTION);
        sB.append(OSM_RESULT_FORMAT).append(Static.EQUAL).append(resultFormat);
        sB.append(Static.AMPERSAND).append(OSM_LATITUDE).append(Static.EQUAL).append(lat);
        sB.append(Static.AMPERSAND).append(OSM_LONGITUDE).append(Static.EQUAL).append(lon);
        sB.append(Static.AMPERSAND).append(OSM_ZOOM).append(Static.EQUAL).append(zoom);
        sB.append(Static.AMPERSAND).append(OSM_DETALIZATION).append(Static.EQUAL).append(detalization);
        sB.append(Static.AMPERSAND).append(OSM_LOCALE).append(Static.EQUAL).append(Locale.getDefault());
        return sB.toString();
    }

    private static String getSearchRequest(String text) {
        StringBuilder sB = new StringBuilder();
        sB.append(OSM_URL).append(Static.SLASH).append(OSM_SEARCH_SUB).append(Static.QUESTION);
        sB.append(OSM_QUERY).append(Static.EQUAL).append(text);
        sB.append(Static.AMPERSAND).append(OSM_RESULT_FORMAT).append(Static.EQUAL).append(resultFormat);
        sB.append(Static.AMPERSAND).append(OSM_POLYGON).append(Static.EQUAL).append(polygon);
        sB.append(Static.AMPERSAND).append(OSM_DETALIZATION).append(Static.EQUAL).append(detalization);
        return sB.toString();
    }

    public static class OSMAddress extends Address {

        private String mAddress;
        private double mLattitude;
        private double mLongitude;

        public OSMAddress(Locale locale, String address, double lat, double lon) {
            super(locale);
            mAddress = address;
            mLattitude = lat;
            mLongitude = lon;
        }

        @Override
        public double getLatitude() {
            return mLattitude;
        }

        @Override
        public double getLongitude() {
            return mLongitude;
        }

        public String getAddress() {
            return mAddress;
        }
    }
}
