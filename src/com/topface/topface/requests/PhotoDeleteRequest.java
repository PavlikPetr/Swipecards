package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class PhotoDeleteRequest extends ApiRequest {
    // Data
    private String service = "photoDelete";
    public int photoid; // идентификатор фотографии для установки в качестве главной
    //---------------------------------------------------------------------------
    public PhotoDeleteRequest(Context context) {
        super(context);
    }
    //---------------------------------------------------------------------------
    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("photoid", photoid));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
    //---------------------------------------------------------------------------
}
