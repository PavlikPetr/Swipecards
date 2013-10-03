package com.topface.topface.requests;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONException;
import org.json.JSONObject;

public class LeaderRequest extends ApiRequest {
    public static final String SERVICE_NAME = "leader.become";
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

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().sendEvent("Leaders", "Buy", "", (long) CacheProfile.getOptions().priceLeader);
    }
}
