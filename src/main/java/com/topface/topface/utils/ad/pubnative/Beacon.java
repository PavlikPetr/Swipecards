package com.topface.topface.utils.ad.pubnative;

import com.google.gson.annotations.SerializedName;

/**
 * Created by saharuk on 15.01.15.
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
