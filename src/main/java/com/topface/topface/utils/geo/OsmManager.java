package com.topface.topface.utils.geo;

import com.topface.framework.utils.Debug;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONObject;

import java.util.Locale;

public class OsmManager {

    public static final String OSM_URL = "http://nominatim.openstreetmap.org";
    public static final String OSM_REVERSE_SUB = "reverse";
    public static final String OSM_RESULT_FORMAT = "format";
    public static final String OSM_LATITUDE = "lat";
    public static final String OSM_LONGITUDE = "lon";
    public static final String OSM_ZOOM = "zoom";
    public static final String OSM_DETALIZATION = "addressdetails";
    public static final String OSM_LOCALE = "accept-language";
    public static final String zoom = "18";
    public static final String detalization = "1";
    public static String resultFormat = "json";
    public static final String SLASH = "/";
    public static final String QUESTION = "?";
    public static final String EQUAL = "=";

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

    private static String getAddressRequest(double lat, double lon) {
        StringBuilder sB = new StringBuilder();
        sB.append(OSM_URL).append(SLASH).append(OSM_REVERSE_SUB).append(QUESTION);
        sB.append(OSM_RESULT_FORMAT).append(EQUAL).append(resultFormat);
        sB.append(Utils.AMPERSAND).append(OSM_LATITUDE).append(EQUAL).append(lat);
        sB.append(Utils.AMPERSAND).append(OSM_LONGITUDE).append(EQUAL).append(lon);
        sB.append(Utils.AMPERSAND).append(OSM_ZOOM).append(EQUAL).append(zoom);
        sB.append(Utils.AMPERSAND).append(OSM_DETALIZATION).append(EQUAL).append(detalization);
        sB.append(Utils.AMPERSAND).append(OSM_LOCALE).append(EQUAL).append(Locale.getDefault());
        return sB.toString();
    }

}
