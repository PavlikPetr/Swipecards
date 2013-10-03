package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("UnusedDeclaration")
public class ChangePasswordRequest extends ApiRequest {

    public static final String SERVICE_NAME = "register.changePassword";

    private String currentPassword;
    private String newPassword;

    public ChangePasswordRequest(Context context, String currentPassword, String newPassword) {
        super(context);
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("current", currentPassword).put("updated", newPassword);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
