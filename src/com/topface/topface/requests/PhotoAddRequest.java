package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoAddRequest extends AbstractApiRequest {
    // Data
    public static final String service = "photoAdd";

    public boolean ero; // флаг, является ли фотография эротической
    public int cost; // стоимость просмотра эротической фотографии

    public PhotoAddRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    protected String getServiceName() {
        return service;
    }
}
