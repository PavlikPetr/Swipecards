package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class NoviceLikesRequest extends ApiRequest {
    // Data
    public static final String service = "noviceLikes";

    public NoviceLikesRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject());
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
