package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoMainRequest extends AbstractApiRequest {
    // Data
    public static final String service = "photoMain";
    public int photoid; // идентификатор фотографии для установки в качестве главной

    public PhotoMainRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("photoid", photoid);
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().trackEvent("Profile", "PhotoSetMain", "", 1L);
    }
}
