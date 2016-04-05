package com.topface.topface.utils.social;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkRequestMode;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ppavlik on 05.04.16.
 * parrent for all request to OK API
 */
public abstract class OkRequest {

    public List<OkRequestListener> mListeners;
    private Odnoklassniki mOdnoklassniki;
    private String mRequestMethod;
    private Map<String, String> mParams;
    private EnumSet<OkRequestMode> mMode;

    public OkRequest(@NotNull Odnoklassniki ok, @NotNull String requestMethod) {
        this(ok, requestMethod, null, null);
    }

    public OkRequest(@NotNull Odnoklassniki ok, @NotNull String requestMethod, @Nullable Map<String, String> params) {
        this(ok, requestMethod, params, null);
    }

    public OkRequest(@NotNull Odnoklassniki ok, @NotNull String requestMethod, @Nullable Map<String, String> params, @Nullable EnumSet<OkRequestMode> mode) {
        mOdnoklassniki = ok;
        mRequestMethod = requestMethod;
        mParams = params;
        mMode = mode;
    }

    @NotNull
    private List<OkRequestListener> getListeners() {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        return mListeners;
    }

    public OkRequest setUserListener(OkRequestListener listener) {
        getListeners().add(listener);
        return this;
    }

    public void exec() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext(mOdnoklassniki.request(mRequestMethod, mParams, mMode));
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(new Throwable());
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        onFail();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Debug.log("Odnoklassniki " + mRequestMethod + " result: " + s);
                        OkUserData data = JsonUtils.fromJson(s, OkUserData.class);
                        if (data != null) {
                            onSuccess(data);
                        } else {
                            onFail();
                        }
                    }
                });
    }

    private void onFail() {
        for (OkRequestListener listener : getListeners()) {
            if (listener != null) {
                listener.onFail();
            }
        }
        onDestroy();
    }

    private void onSuccess(@Nullable OkUserData data) {
        for (OkRequestListener listener : getListeners()) {
            if (listener != null) {
                listener.onSuccess(data);
            }
        }
        onDestroy();
    }

    private void onDestroy() {
        getListeners().clear();
    }

    public interface OkRequestListener {

        void onSuccess(@Nullable OkUserData data);

        void onFail();

    }
}
