package com.topface.topface.utils.ad;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.http.HttpUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for native ad managing.
 */
public abstract class Advertising {

    private boolean mIsLoading;
    protected abstract String requestUrl();

    private ArrayList<NativeAd> mNativeAds = new ArrayList<>();

    public void requestAd() {
        if (needMoreAds()) {
            mIsLoading = true;
            new BackgroundThread() {
                @Override
                public void execute() {
                    String adsResponse = null;
                    for (int i = 0; i < ApiRequest.MAX_RESEND_CNT && adsResponse == null; i++) {
                        String request = requestUrl();
                        Debug.log("NativeAd: " + getClass().getSimpleName() + " sent request:\n" + request);
                        adsResponse = HttpUtils.httpGetRequest(request);
                        Debug.log("NativeAd: " + getClass().getSimpleName() + " received response:\n" + adsResponse);
                    }
                    if (adsResponse != null) {
                        mNativeAds.addAll(parseResponse(adsResponse));
                    }
                    mIsLoading = false;
                }
            };
        }
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    protected abstract List<NativeAd> parseResponse(String response);

    public boolean needMoreAds() {
        int remainedShows = getRemainedShows();
        return isEnabled() && mNativeAds.size() < remainedShows;
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

    public abstract int getRemainedShows();

    public abstract boolean isEnabled();
}
