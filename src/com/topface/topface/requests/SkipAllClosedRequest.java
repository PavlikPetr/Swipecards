package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SkipAllClosedRequest extends ApiRequest {

    public static final String service = "skipAllClosed";

    public static final int LIKES = 0;
    public static final int MUTUAL = 1;

    public int type;

    public SkipAllClosedRequest(int type, Context context) {
        super(context);
        this.type = type;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("type", type);
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        super.exec();
    }
}
