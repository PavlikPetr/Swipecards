package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoAddRequest extends ApiRequest {
    // Data
    private String service = "photoAdd";

    public boolean ero; // флаг, является ли фотография эротической
    public int cost; // стоимость просмотра эротической фотографии

    public PhotoAddRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("ero", ero).put("cost", cost));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
