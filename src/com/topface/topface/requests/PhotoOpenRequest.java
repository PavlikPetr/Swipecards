package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoOpenRequest extends ApiRequest {
    // Data
    private String service = "photoOpen";
    public int uid;    // идентификатор пользователя хозяина фотографии
    public int photo;  // идентификатор эротической фотографии

    public PhotoOpenRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("uid", uid)
                    .put("photo", photo));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}
