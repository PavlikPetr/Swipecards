package com.topface.topface.utils.ad.pubnative;

import com.google.gson.annotations.SerializedName;

/**
 * Holds url to let pubnative know that we showed their ad.
 */
public class Beacon {

    public static final String IMPRESSION = "impression";

    @SerializedName("type")
    private String mType;
    @SerializedName("url")
    private String mUrl;

    public String getType() {
        return mType;
    }

    public String getUrl() {
        return mUrl;
    }
}
