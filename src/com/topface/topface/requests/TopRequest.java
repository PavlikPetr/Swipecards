package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class TopRequest extends ApiRequest {
    // Data
    private String service = "top";
    public int sex; // пол самых красивых 
    public int city; // город самых красивых

    public TopRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();

        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("sex", sex).put("city", city));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
