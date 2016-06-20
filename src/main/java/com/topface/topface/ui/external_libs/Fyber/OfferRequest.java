package com.topface.topface.ui.external_libs.Fyber;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.topface.framework.JsonUtils;
import com.topface.topface.ui.bonus.models.FyberOfferwallModel;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.OkThrowable;

import java.io.IOException;
import java.lang.reflect.Type;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.topface.topface.utils.social.OkThrowable.OkThrowableType.EMPTY_RESPONSE;

public class OfferRequest {
    private static final String OFFER_LINK="http://api.fyber.com/feed/v1/offers.json?";
    private static final String APP_ID_KEY = "appid";
    private static final String UID_KEY = "uid";
    private static final String LOCALE_KEY = "locale";
    private static final String FORMAT_KEY = "format";
    private static final String OS_VERSION_KEY = "os_version";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String HASHKEY_KEY = "hashkey";
    private static final String GOOGLE_AD_ID_KEY = "google_ad_id";
    private static final String IS_LIMITED_KEY = "google_ad_id_limited_tracking_enabled";
    private static final String OFFER_TYPES_KEY = "offer_types";
    private static final String PAGE_KEY = "page";

    private static final String APP_ID = "11625";

    private Subscriber mSubscriber;

    public Observable<FyberOfferwallModel> getObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mSubscriber = subscriber;
                Ht
                try {
                    res = mOdnoklassniki.request(getRequestMethod(), getRequestParams(), getRequestMode());
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onNext(res);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        if (TextUtils.isEmpty(s) && mSubscriber != null) {
                            mSubscriber.onError(new OkThrowable(EMPTY_RESPONSE));
                        }
                        return !TextUtils.isEmpty(s);
                    }
                }).doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        if (mSubscriber != null && !mSubscriber.isUnsubscribed()) {
                            mSubscriber.unsubscribe();
                        }
                    }
                }).map(new Func1<String, T>() {
                    @Override
                    public T call(String s) {
                        Type type = getDataType();
                        // хак, чтобы вернуть строку (json) без парсинга
                        return type == new TypeToken<String>() {
                        }.getType() ? (T) s : (T) JsonUtils.fromJson(s, type);
                    }
                });
    }
}
