package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ErrorCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OkMarkRenewalAsSentRequest extends ApiRequest {
    public static final String SERVICE_NAME = "ok.markRenewalAsSent";

    private String[] mIds;

    public OkMarkRenewalAsSentRequest(Context context, String id) {
        super(context);
        mIds = new String[]{id};
    }

    @Override
    public void exec() {
        if (mIds != null && mIds.length > 0) {
            super.exec();
        }
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        if (mIds != null && mIds.length > 0) {
            for (String id : mIds) {
                array.put(id);
            }
        }
        json.put("ids", array);
        return json;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
