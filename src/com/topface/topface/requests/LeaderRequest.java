package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class LeaderRequest extends AbstractApiRequest {
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
    protected String getServiceName() {
        return "leader";
    }
}
