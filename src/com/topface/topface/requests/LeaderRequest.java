package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class LeaderRequest extends ApiRequest {

    private static final String service = "leader";
    private int mPhotoId;

    public LeaderRequest(int photoId, Context context) {
        super(context);
        mPhotoId = photoId;
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("photo", mPhotoId);
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
