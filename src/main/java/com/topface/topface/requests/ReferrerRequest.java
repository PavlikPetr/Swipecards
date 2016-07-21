package com.topface.topface.requests;


import android.content.Context;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.data.InstallReferrerData;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class ReferrerRequest extends ApiRequest {
    public static final String SERVICE_NAME = "referral.track";

    private AdjustAttributeData mAttribution;
    private InstallReferrerData mReferrerTrack;

    public ReferrerRequest(Context context, @NotNull AdjustAttributeData attribution) {
        super(context);
        mAttribution = attribution;
    }

    public ReferrerRequest(Context context, @NotNull InstallReferrerData referrerTrack) {
        super(context);
        mReferrerTrack = referrerTrack;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (mAttribution != null) {
            jsonObject.put("adjust", JsonUtils.toJson(mAttribution));
        }
        if (!InstallReferrerData.isEmpty(mReferrerTrack)) {
            jsonObject.put("installReferrer", mReferrerTrack.getInstallReferrerTrackData());
        }
        return jsonObject;
    }

    @Override
    public void exec() {
        if (mAttribution == null && InstallReferrerData.isEmpty(mReferrerTrack)) {
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
