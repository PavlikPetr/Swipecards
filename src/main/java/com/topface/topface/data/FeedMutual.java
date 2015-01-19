package com.topface.topface.data;

import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONObject;

public class FeedMutual extends FeedLike {

    public FeedMutual() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public FeedMutual(JSONObject data) {
        super(data);
    }

    public FeedMutual(NativeAd nativeAd) {
        super(nativeAd);
    }
}
