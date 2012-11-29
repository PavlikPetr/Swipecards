package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class CoordinatesRequest extends AbstractApiRequest {
    public static final int COORDINATES_TYPE_SELF = 1;
    public static final int COORDINATES_TYPE_PLACE = 2;
    // Data
    public static final String SERVICE_NAME = "coordinates";
    public int userid; // идентификатор пользователя, кому послали сообщение
    public double longitude; // долгота отправляемого местоположения -180,+180
    public double latitude; // широта отправляемого местоположения -90,+90
    public int type;
    public String address;

    public CoordinatesRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("receiverid", userid)
                .put("longitude", longitude)
                .put("latitude", latitude)
                .put("type", type)
                .put("message", address);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
