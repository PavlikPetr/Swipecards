package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class CoordinatesRequest extends ApiRequest {
    public static final int COORDINATES_TYPE_SELF = 1;
    public static final int COORDINATES_TYPE_PLACE = 2;
    // Data
    private String service = "coordinates";
    public int userid; // идентификатор пользователя, кому послали сообщение
    public double longitude; // долгота отправляемого местоположения -180,+180
    public double latitude; // широта отправляемого местоположения -90,+90
    public int type;
    public String address;

    public CoordinatesRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject()
                    .put("receiverid", userid)
                    .put("longitude", longitude)
                    .put("latitude", latitude)
                    .put("type", type)
                    .put("message", address));
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
}
