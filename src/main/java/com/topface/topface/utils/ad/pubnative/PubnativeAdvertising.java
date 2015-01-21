package com.topface.topface.utils.ad.pubnative;

import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.topface.framework.JsonUtils;
import com.topface.offerwall.common.TFCredentials;
import com.topface.topface.App;
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

    private PubnativeInfo mPubnativeInfo;
    private LinkedList<NativeAd> mAds;

    public PubnativeAdvertising() {
        DisplayMetrics metrics = App.getContext().getResources().getDisplayMetrics();
        String locale = new LocaleConfig(App.getContext()).getApplicationLocale();
        PubnativeInfo.Builder pubnativeBuilder = new PubnativeInfo.Builder().displayMetrics(metrics).
                adId(TFCredentials.getAdId(App.getContext())).locale(locale);
        pubnativeBuilder.location(App.getLastKnownLocation());
        mPubnativeInfo = pubnativeBuilder.create();
    }

    @Override
    protected String requestUrl() {
        return REQUEST + mPubnativeInfo.asRequestParameters();
    }

    @Override
    protected List<NativeAd> parseResponse(String response) {
        mAds = new LinkedList<>();
        PubnativeResponse pubnativeResponse = JsonUtils.fromJson(response, PubnativeResponse.class);
        if (TextUtils.equals(pubnativeResponse.getStatus(), "ok")) {
            PubnativeAd[] ads = pubnativeResponse.getAds();
            if (ads != null) {
                for (PubnativeAd ad : ads) {
                    if (ad.isValid()) {
                        mAds.add(ad);
                    }
                }
            }
        }
        return mAds;
    }

    @Override
    public int getRemainedShows() {
        return App.getUserConfig().getRemainedPubnativeShows();
    }
}
