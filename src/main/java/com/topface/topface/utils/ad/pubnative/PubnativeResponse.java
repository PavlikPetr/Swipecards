package com.topface.topface.utils.ad.pubnative;

/**
 * Response for pubnative ad request
 */
public class PubnativeResponse {

    @SuppressWarnings("UnusedDeclaration")
    private String status;
    @SuppressWarnings("UnusedDeclaration")
    private PubnativeAd[] ads;

    public String getStatus() {
        return status;
    }

    public PubnativeAd[] getAds() {
        return ads;
    }
}
