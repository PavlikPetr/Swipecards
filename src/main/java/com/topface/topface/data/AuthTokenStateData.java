package com.topface.topface.data;

import android.support.annotation.IntDef;

public class AuthTokenStateData {
    public static final int TOKEN_STATUS_UNDEFINED = 0;
    public static final int TOKEN_READY = 1;
    public static final int TOKEN_FAILED = 2;
    public static final int TOKEN_NOT_READY = 3;
    public static final int TOKEN_PREPARING = 4;
    public static final int TOKEN_AUTHORIZED = 5;

    @IntDef({TOKEN_STATUS_UNDEFINED, TOKEN_READY, TOKEN_FAILED, TOKEN_NOT_READY, TOKEN_PREPARING, TOKEN_AUTHORIZED})
    public @interface AuthTokenStatus {
    }

    @AuthTokenStatus
    private int mStatus;

    public AuthTokenStateData() {
        this(TOKEN_STATUS_UNDEFINED);
    }

    public AuthTokenStateData(@AuthTokenStatus int status) {
        mStatus = status;
    }

    @AuthTokenStatus
    public int getStatus() {
        return mStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AuthTokenStateData)) return false;
        AuthTokenStateData data = (AuthTokenStateData) o;
        return data.getStatus() == getStatus();
    }

    @Override
    public int hashCode() {
        return mStatus;
    }
}
