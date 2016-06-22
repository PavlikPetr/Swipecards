package com.topface.topface.ui.external_libs.Fyber;

import android.os.Build;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.ui.bonus.models.FyberOffersResponse;
import com.topface.topface.ui.bonus.models.OfferwallBaseModel;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.HttpUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.adjust.sdk.Util.convertToHex;

public class OfferRequest {
    private static final String OFFER_LINK = "http://api.fyber.com/feed/v1/offers.json?";
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

    private OnObservablePrepare mObservableCallback;

    public void prepareObservable(OnObservablePrepare callback) {
        mObservableCallback = callback;
        prepareRequestParams();
    }

    private Single<List<? extends OfferwallBaseModel>> getObservable(final String request) {
        return Single.create(new Single.OnSubscribe<List<? extends OfferwallBaseModel>>() {
            @Override
            public void call(SingleSubscriber<? super List<? extends OfferwallBaseModel>> singleSubscriber) {
                String response = HttpUtils.httpGetRequest(request);
                if (TextUtils.isEmpty(response)) {
                    singleSubscriber.onError(new IOException("Response is empty"));
                } else {
                    FyberOffersResponse data = JsonUtils.fromJson(response, FyberOffersResponse.class);
                    if (data.getCode().equals("OK")) {
                        singleSubscriber.onSuccess(data.getOffers());
                    } else {
                        singleSubscriber.onError(new IOException("Request return fail"));
                    }
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void prepareRequestParams() {
        final TreeMap<String, String> params = new TreeMap<>();
        params.put(APP_ID_KEY, APP_ID);
        params.put(UID_KEY, String.valueOf(App.get().getProfile().uid));
        params.put(LOCALE_KEY, App.getCurrentLocale().getLanguage());
        params.put(FORMAT_KEY, RESPONSE_FORMAT);
        params.put(OS_VERSION_KEY, Build.VERSION.RELEASE);
        params.put(TIMESTAMP_KEY, String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000L));
        getGoogleAdIdObservable().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                params.put(GOOGLE_AD_ID_KEY, s);
                if (params.containsKey(IS_LIMITED_KEY)) {
                    prepareRequest(params);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                params.put(GOOGLE_AD_ID_KEY, Utils.EMPTY);
                if (params.containsKey(IS_LIMITED_KEY)) {
                    prepareRequest(params);
                }
            }
        });
        getGoogleAdIdTrackingEnabledObservable().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean isEnable) {
                params.put(IS_LIMITED_KEY, String.valueOf(isEnable));
                if (params.containsKey(GOOGLE_AD_ID_KEY)) {
                    prepareRequest(params);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                params.put(IS_LIMITED_KEY, String.valueOf(false));
                if (params.containsKey(GOOGLE_AD_ID_KEY)) {
                    prepareRequest(params);
                }
            }
        });
    }

    private void prepareRequest(TreeMap<String, String> params) {
        String request = Utils.EMPTY;
        int pos = 0;
        Locale currentLocale = App.getCurrentLocale();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            request = request.concat(String.format(currentLocale, pos == 0 ? FIRST_KEY_TEMPLATE : KEY_TEMPLATE, entry.getKey(), entry.getValue()));
            pos++;
        }
        request = request.concat(String.format(currentLocale, KEY_TEMPLATE, HASHKEY_KEY, getSHA1(request.concat(Utils.AMPERSAND).concat(API_KEY))));
        request = OFFER_LINK.concat(request);
        Debug.showChunkedLogError("OfferRequestTest", "" + request);
        if (mObservableCallback != null) {
            mObservableCallback.observablePrepared(getObservable(request));
        }
    }

    private Single<String> getGoogleAdIdObservable() {
        return Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(SingleSubscriber<? super String> singleSubscriber) {
                try {
                    singleSubscriber.onSuccess(AdvertisingIdClient.getAdvertisingIdInfo(App.getContext()).getId());
                } catch (IOException
                        | GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    singleSubscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Single<Boolean> getGoogleAdIdTrackingEnabledObservable() {
        return Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
                try {
                    singleSubscriber.onSuccess(AdvertisingIdClient.getAdvertisingIdInfo(App.getContext()).isLimitAdTrackingEnabled());
                } catch (IOException
                        | GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    singleSubscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
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

    public interface OnObservablePrepare {
        void observablePrepared(Single<List<? extends OfferwallBaseModel>> observable);
    }
}
