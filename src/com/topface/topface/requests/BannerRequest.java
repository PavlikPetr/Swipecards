package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class BannerRequest extends ApiRequest {
    // Data
    private String service = "banner";
    public String place;
    // идентификатор места отображения баннера. Возможные значения: LIKE, MUTUAL, MESSAGES, TOP
    // Constants
    public static final String LIKE = "LIKE";
    //public static final String MUTUAL = "MUTUAL";
    public static final String MESSAGES = "MESSAGES";
    public static final String TOP = "TOP";
    public static final String VISITORS = "VISITORS";

    public BannerRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("place", place));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}
