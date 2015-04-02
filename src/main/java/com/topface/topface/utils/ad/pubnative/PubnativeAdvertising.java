package com.topface.topface.utils.ad.pubnative;

import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.google.gson.JsonSyntaxException;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.TFCredentials;
import com.topface.topface.App;
import com.topface.topface.utils.CacheProfile;
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
    private static final String ODNOKLASSNIKI = "ok";

    private PubnativeInfo mPubnativeInfo;

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
        List<NativeAd> nativeAds = new LinkedList<>();
        try {
            PubnativeResponse pubnativeResponse = JsonUtils.fromJson(response, PubnativeResponse.class);
            if (pubnativeResponse != null
                    && TextUtils.equals(pubnativeResponse.getStatus(), ODNOKLASSNIKI)) {
                PubnativeAd[] ads = pubnativeResponse.getAds();
                if (ads != null) {
                    for (PubnativeAd ad : ads) {
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
        return App.getUserConfig().getRemainedPubnativeShows();
    }

    @Override
    public boolean isEnabled() {
        return CacheProfile.getOptions().feedNativeAd.enabled;
    }
}
