package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class CoordinatesRequest extends ApiRequest {
    public static final int COORDINATES_TYPE_SELF = 1;
    public static final int COORDINATES_TYPE_PLACE = 2;
    // Data
    public static final String SERVICE_NAME = "message.sendCoordinates";
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
                .put("receiverId", userid)
                .put("longitude", longitude)
                .put("latitude", latitude)
                .put("type", type)
                .put("message", address);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().sendEvent("Feed", "CoordinatesSend", "Type" + String.valueOf(type), 1L);
    }

}
