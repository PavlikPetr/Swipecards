package com.topface.topface.ui.external_libs.offers.Fyber;

import android.os.Build;

import com.topface.topface.App;
import com.topface.topface.ui.external_libs.offers.GoogleAdParams;
import com.topface.topface.ui.external_libs.offers.OffersUtils;
import com.topface.topface.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.functions.Func1;

import static com.adjust.sdk.Util.convertToHex;

public class FyberOffersRequest {
    private static final String BASE_FYBER_LINK = "http://api.fyber.com/";
    private static final String GET_OFFERS_LINK = "feed/v1/offers.json";
    private static final String APP_ID_KEY = "appid";
    private static final String UID_KEY = "uid";
    private static final String LOCALE_KEY = "locale";
    private static final String FORMAT_KEY = "format";
    private static final String OS_VERSION_KEY = "os_version";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String HASHKEY_KEY = "hashkey";
    private static final String GOOGLE_AD_ID_KEY = "google_ad_id";
    private static final String IS_LIMITED_KEY = "google_ad_id_limited_tracking_enabled";

    private static final String APP_ID = "11625";
    private static final String API_KEY = "fd4047e6bf8b7ab7f64e936965f1144c27e81150";
    private static final String FIRST_KEY_TEMPLATE = "%s=%s";
    private static final String KEY_TEMPLATE = "&%s=%s";
    private static final String RESPONSE_FORMAT = "json";

    public Observable<FyberOffersResponse> getRequestObservable() {
        return OffersUtils.getGoogleAdParamsObservable()
                .map(new Func1<GoogleAdParams, Map<String, String>>() {
                    @Override
                    public Map<String, String> call(GoogleAdParams googleAdParams) {
                        TreeMap<String, String> params = new TreeMap<>();
                        params.put(APP_ID_KEY, APP_ID);
                        params.put(UID_KEY, String.valueOf(App.get().getProfile().uid));
                        params.put(LOCALE_KEY, App.getCurrentLocale().getLanguage());
                        params.put(FORMAT_KEY, RESPONSE_FORMAT);
                        params.put(OS_VERSION_KEY, Build.VERSION.RELEASE);
                        params.put(TIMESTAMP_KEY, String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000L));
                        params.put(IS_LIMITED_KEY, String.valueOf(googleAdParams.isLimitAdTrackingEnabled()));
                        params.put(GOOGLE_AD_ID_KEY, googleAdParams.getId());
                        params.put(HASHKEY_KEY, getHash(params));
                        return params;
                    }
                })
                .flatMap(new Func1<Map<String, String>, Observable<FyberOffersResponse>>() {
                    @Override
                    public Observable<FyberOffersResponse> call(Map<String, String> params) {
                        return OffersUtils.getRequestInstance(BASE_FYBER_LINK).create(OffersRequest.class).setParams(params);
                    }
                });
    }

    private interface OffersRequest {
        @GET(GET_OFFERS_LINK)
        Observable<FyberOffersResponse> setParams(@QueryMap Map<String, String> params);
    }


    private String getHash(TreeMap<String, String> params) {
        String request = Utils.EMPTY;
        int pos = 0;
        Locale currentLocale = App.getCurrentLocale();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            request = request.concat(String.format(currentLocale, pos == 0 ? FIRST_KEY_TEMPLATE : KEY_TEMPLATE, entry.getKey(), entry.getValue()));
            pos++;
        }
        return getSHA1(request.concat(Utils.AMPERSAND).concat(API_KEY));
    }

    private String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        return convertToHex(md.digest());
    }

    private String getSHA1(String text) {
        String res = Utils.EMPTY;
        try {
            res = SHA1(text);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }
}
