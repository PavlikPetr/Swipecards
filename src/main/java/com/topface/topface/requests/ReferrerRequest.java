package com.topface.topface.requests;


import android.content.Context;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.InstallReferrerData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class ReferrerRequest extends ApiRequest {
    public static final String SERVICE_NAME = "referral.track";

    private InstallReferrerData mReferrerTrack;
    private String mKochavaAttributionData;

    public ReferrerRequest(@NotNull Context context, @NotNull InstallReferrerData referrerTrack) {
        super(context);
        mReferrerTrack = referrerTrack;
    }

    public ReferrerRequest(@NotNull Context context, @NotNull String attributionData) {
        super(context);
        mKochavaAttributionData = attributionData;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (!InstallReferrerData.isEmpty(mReferrerTrack)) {
            jsonObject.put("installReferrer", mReferrerTrack.getInstallReferrerTrackData());
        }
        if (!TextUtils.isEmpty(mKochavaAttributionData)) {
            jsonObject.put("kochava", mKochavaAttributionData);
        }
        return jsonObject;
    }

    @Override
    public void exec() {
        if (InstallReferrerData.isEmpty(mReferrerTrack) && TextUtils.isEmpty(mKochavaAttributionData)) {
            Debug.error("Request data is empty");
            return;
        }
        super.exec();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
