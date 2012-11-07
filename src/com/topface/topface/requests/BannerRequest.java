package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class BannerRequest extends AbstractApiRequest {
    // Data
    public static final String SERVICE_NAME = "banner";
    public String place; // идентификатор места отображения баннера. Возможные значения: LIKE, MUTUAL, MESSAGES, TOP
    // Constants
    public static final String TOP = "TOP";
    public static final String LIKE = "LIKE";
    public static final String INBOX = "MESSAGES";
    public static final String SYMPATHY = "MUTUAL";

    public BannerRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("place", place);
    }

    @Override
    protected String getServiceName() {
        return SERVICE_NAME;
    }

}
