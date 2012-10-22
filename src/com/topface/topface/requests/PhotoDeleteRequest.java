package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoDeleteRequest extends ApiRequest {
    // Data
    private String service = "photoDelete";
    public int[] photos;

    public PhotoDeleteRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("photoid", getPhotosJson()));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

    private JSONArray getPhotosJson() throws JSONException {
        JSONArray photosJson = new JSONArray();
        for (int photo : photos) {
            photosJson.put(photo);
        }
        return photosJson;
    }
}
