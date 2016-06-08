package com.topface.topface.data;

import android.support.annotation.IntDef;

public class AuthStateData {
    public static final int TOKEN_STATUS_UNDEFINED = 0;
    public static final int TOKEN_READY = 1;
    public static final int TOKEN_FAILED = 2;
    public static final int TOKEN_NOT_READY = 3;
    public static final int TOKEN_PREPARING = 4;

    public static final int AUTH_TYPE_UNDEFINED = 0;
    public static final int FB = 1;
    public static final int VK = 2;
    public static final int OK = 3;
    public static final int TF = 4;

    @IntDef({TOKEN_STATUS_UNDEFINED, TOKEN_READY, TOKEN_FAILED, TOKEN_NOT_READY, TOKEN_PREPARING})
    public @interface AuthTokenStatus {
    }

    @IntDef({AUTH_TYPE_UNDEFINED, FB, VK, OK, TF})
    public @interface AuthType {
    }

    @AuthTokenStatus
    private int mStatus;
    @AuthType
    private int mAuthType;

    public AuthStateData() {
        this(TOKEN_STATUS_UNDEFINED, AUTH_TYPE_UNDEFINED);
    }

    public AuthStateData(@AuthTokenStatus int status, @AuthType int authType) {
        mStatus = status;
        mAuthType = authType;
    }

    @AuthTokenStatus
    public int getStatus() {
        return mStatus;
    }

    @AuthType
    public int getAuthType() {
        return mAuthType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AuthStateData)) return false;
        AuthStateData data = (AuthStateData) o;
        return data.getStatus() == getStatus() && data.getAuthType() == getAuthType();
    }

    @Override
    public int hashCode() {
        int res = mStatus;
        return res * 31 + mAuthType;
    }
}
