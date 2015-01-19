package com.topface.topface.utils.ad.pubnative;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.topface.framework.utils.Debug;
import com.topface.offerwall.common.TFCredentials;
import com.topface.topface.App;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.ad.Advertising;
import com.topface.topface.utils.ad.NativeAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Managing pubnative ad.
 */
public class PubnativeAdvertising extends Advertising {

    private static final String REQUEST = "http://api.pubnative.net/api/partner/v2/promotions/native?";

    private PubnativeInfo mPubnativeInfo;

    public PubnativeAdvertising() {
        DisplayMetrics metrics = App.getContext().getResources().getDisplayMetrics();
        LocationManager location = (LocationManager) App.getContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        List<String> locationProviders = location.getProviders(criteria, true);
        String locale = new LocaleConfig(App.getContext()).getApplicationLocale();
        PubnativeInfo.Builder pubnativeBuilder = new PubnativeInfo.Builder().displayMetrics(metrics).
                adId(TFCredentials.getAdId(App.getContext())).locale(locale);
        if (!locationProviders.isEmpty()) {
            pubnativeBuilder.location(location.getLastKnownLocation(locationProviders.get(0)));
        }
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
            JSONObject adsJson = new JSONObject(response);
            if (TextUtils.equals(adsJson.optString("status"), "ok")) {
                JSONArray ads = adsJson.optJSONArray("ads");
                if (ads != null) {
                    Gson gson = new Gson();
                    for (int i = 0; i < ads.length(); i++) {
                        nativeAds.add(gson.fromJson(ads.get(i).toString(), PubnativeAd.class));
                    }
                }
            }
        } catch (JSONException e) {
            Debug.error("Wrong pubnative response JSON", e);
        }
        return nativeAds;
    }

    @Override
    public boolean hasShowsRemained() {
        int remainedShows = App.getUserConfig().getRemainedPubnativeShows();
        return remainedShows > 0;
    }
}
