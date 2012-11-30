package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoAddRequest extends AbstractApiRequest {
    // Data
    public static final String service = "photoAdd";

    public PhotoAddRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().trackEvent("Profile", "PhotoAdd", "", 1L);
    }
}
