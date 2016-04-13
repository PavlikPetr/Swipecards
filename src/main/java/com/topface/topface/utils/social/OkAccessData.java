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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OkAccessData)) return false;
        OkAccessData okAccessData = (OkAccessData) o;
        return mAccessToken.equals(okAccessData.getToken()) && mSessionSecretKey.equals(okAccessData.getSecretKey());
    }

    @Override
    public int hashCode() {
        int result = mAccessToken != null ? mAccessToken.hashCode() : 0;
        result = 31 * result + (mSessionSecretKey != null ? mSessionSecretKey.hashCode() : 0);
        return result;
    }
}
