package com.topface.topface.requests;

import android.content.Context;

import com.topface.framework.utils.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * inform TF server that renewed subscription was marked on OK side
 */
public class OkMarkRenewalAsSentRequest extends ApiRequest {
    public static final String SERVICE_NAME = "ok.markRenewalAsSent";

    private String[] mIds;

    public OkMarkRenewalAsSentRequest(Context context, String... id) {
        super(context);
        mIds = id;
    }

    @Override
    public void exec() {
        if (mIds != null && mIds.length > 0) {
            super.exec();
        } else {
            Debug.error("Subscriptions array are empty, request canceled");
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
