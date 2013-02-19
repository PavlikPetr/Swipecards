package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogDeleteRequest extends ApiRequest {
    private static final String SERVICE_NAME = "dialogDelete";
    private final int mUserId;

    public DialogDeleteRequest(int userId, Context context) {
        super(context);
        mUserId = userId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userid", mUserId);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        if (mUserId > 0) {
            super.exec();
        } else {
            handleFail(ApiResponse.MISSING_REQUIRE_PARAMETER, "Wrong userid value. userid must be more than 0");
        }
    }
}
