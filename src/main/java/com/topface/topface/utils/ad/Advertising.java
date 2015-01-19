package com.topface.topface.utils.ad;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.http.HttpUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for native ad managing.
 */
public abstract class Advertising {

    protected abstract String requestUrl();

    private ArrayList<NativeAd> mNativeAds = new ArrayList<>();

    public void requestAd() {
        new BackgroundThread() {
            @Override
            public void execute() {
                String adsResponse = null;
                for (int i = 0; i < ApiRequest.MAX_RESEND_CNT && adsResponse == null; i++) {
                    adsResponse = HttpUtils.httpGetRequest(requestUrl());
                }
                if (adsResponse != null) {
                    mNativeAds.addAll(parseResponse(adsResponse));
                }
            }
        };
    }

    protected abstract List<NativeAd> parseResponse(String response);

    public void addAds(List<NativeAd> ads) {
        mNativeAds.addAll(ads);
    }

    public boolean hasAd() {
        return !mNativeAds.isEmpty();
    }

    public NativeAd popAd() {
        if (hasAd()) {
            NativeAd ad = mNativeAds.get(0);
            mNativeAds.remove(ad);
            return ad;
        }
        return null;
    }

    public abstract boolean hasShowsRemained();
}
