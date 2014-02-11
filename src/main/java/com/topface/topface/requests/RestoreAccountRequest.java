package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.social.AuthToken;

public class RestoreAccountRequest extends AuthRequest {
    public static final String SERVICE = "user.restore";

    public RestoreAccountRequest(AuthToken.TokenInfo tokenInfo, Context context) {
        super(tokenInfo, context);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
