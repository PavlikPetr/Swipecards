package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class DeleteRequest extends ApiRequest {
    private String mId;

    public DeleteRequest(String id, Context context) {
        super(context);
        mId = id;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("item", mId);
        return data;
    }

    @Override
    public String getServiceName() {
        return "feedDelete";
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().trackEvent("Feed", "Delete", "", 1L);
    }
}
