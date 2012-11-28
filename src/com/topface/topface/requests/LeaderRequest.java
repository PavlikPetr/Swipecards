package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class LeaderRequest extends AbstractApiRequest {
    public static final String SERVICE_NAME = "leader";
    private int mPhotoId;

    public LeaderRequest(int photoId, Context context) {
        super(context);
        mPhotoId = photoId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("photo", mPhotoId);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
