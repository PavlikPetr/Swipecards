package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class BannerRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "banner";
    public String place; // идентификатор места отображения баннера. Возможные значения: LIKE, MUTUAL, MESSAGES, TOP

    public BannerRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("place", place);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
