package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;

import android.content.Context;

public class AlbumRequest extends ApiRequest {
    // Data
    private String service = "album";
    public int uid; // внутренний идентификатор пользователя

    public AlbumRequest(Context context) {
        super(context);
    }    

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("uid", uid));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}
