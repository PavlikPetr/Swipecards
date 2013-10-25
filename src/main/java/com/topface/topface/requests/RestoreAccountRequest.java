package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.social.AuthToken;

import org.json.JSONException;
import org.json.JSONObject;

public class RestoreAccountRequest extends AuthRequest{
    public static final String SERVICE = "user.restore";

    public RestoreAccountRequest(AuthToken.TokenInfo tokenInfo, Context context) {
        super(tokenInfo, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
