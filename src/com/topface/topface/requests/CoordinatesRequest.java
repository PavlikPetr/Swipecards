package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class CoordinatesRequest extends ApiRequest {
    // Data
    private String service = "coordinates";
    public int userid; // идентификатор пользователя, кому послали сообщение
    public double longitude; // долгота отправляемого местоположения -180,+180
    public double latitude; // широта отправляемого местоположения -90,+90

    public CoordinatesRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("userid", userid).put("longitude", longitude).put("latitude", latitude));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
