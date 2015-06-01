package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class MarkSMSInviteRequest extends ApiRequest {
    public static final String SERVICE_NAME = "virus.markSmsInviteSent";
    int mId;

    public MarkSMSInviteRequest(Context context, int id) {
        super(context);
        mId = id;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("inviteId", mId);
        return result;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
