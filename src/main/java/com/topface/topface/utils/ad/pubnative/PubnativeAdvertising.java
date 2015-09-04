package com.topface.topface.utils.ad.pubnative;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.google.gson.JsonSyntaxException;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.TFCredentials;
import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.ad.Advertising;
import com.topface.topface.utils.ad.NativeAd;

import java.util.LinkedList;
import java.util.List;

/**
 * Managing pubnative ad.
 */
public class PubnativeAdvertising extends Advertising {

    private static final String REQUEST = "http://api.pubnative.net/api/partner/v2/promotions/native?";
    private static final String OK = "ok";
    private Options mOptions;

    private PubnativeInfo mPubnativeInfo;

    public PubnativeAdvertising(Options options, Context context) {
        mOptions = options;
        DisplayMetrics metrics = App.getContext().getResources().getDisplayMetrics();
        String locale = new LocaleConfig(App.getContext()).getApplicationLocale();
        PubnativeInfo.Builder pubnativeBuilder = new PubnativeInfo.Builder().displayMetrics(metrics).
                adId(TFCredentials.getAdId(App.getContext())).locale(locale);
        pubnativeBuilder.location(App.getLastKnownLocation());
        mPubnativeInfo = pubnativeBuilder.create(options.feedNativeAd.dailyShows, context);
    }

    @Override
    protected String requestUrl() {
        return REQUEST + mPubnativeInfo.asRequestParameters();
    }

    @Override
    protected List<NativeAd> parseResponse(String response) {
        List<NativeAd> nativeAds = new LinkedList<>();
        try {
            PubnativeResponse pubnativeResponse = JsonUtils.fromJson(response, PubnativeResponse.class);
            if (pubnativeResponse != null
                    && TextUtils.equals(pubnativeResponse.getStatus(), OK)) {
                PubnativeAd[] ads = pubnativeResponse.getAds();
                if (ads != null) {
                    for (PubnativeAd ad : ads) {
                        ad.setPosition(mOptions.feedNativeAd.getPosition());
                        if (ad.isValid()) {
                            nativeAds.add(ad);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException ex) {
            Debug.error(ex.toString());
        }
        return nativeAds;
    }

    @Override
    public int getRemainedShows() {
        return App.getUserConfig().getRemainedPubnativeShows(mOptions.feedNativeAd.dailyShows);
    }

    @Override
    public boolean isEnabled() {
        return mOptions.feedNativeAd.enabled;
    }
}
