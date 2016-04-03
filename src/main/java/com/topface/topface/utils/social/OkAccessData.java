package com.topface.topface.utils.social;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Петр on 03.04.2016.
 * Hold token and secret key
 */
public class OkAccessData {
    @SerializedName("access_token")
    private String mAccessToken = null;
    @SerializedName("session_secret_key")
    private String mSessionSecretKey = null;

    public OkAccessData(String accessToken, String sessionSecretKey) {
        mAccessToken = accessToken;
        mSessionSecretKey = sessionSecretKey;
    }

    public String getToken() {
        return mAccessToken;
    }

    public String getSecretKey() {
        return mSessionSecretKey;
    }

    public boolean isEmpty() {
        return mAccessToken == null && mSessionSecretKey == null;
    }
}
