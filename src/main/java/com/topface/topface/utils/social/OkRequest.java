package com.topface.topface.utils.social;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.topface.framework.JsonUtils;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import ru.ok.android.sdk.Odnoklassniki;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.topface.topface.utils.social.OkThrowable.OkThrowableType.EMPTY_RESPONSE;

/**
 * Created by ppavlik on 05.04.16.
 * parrent for all request to OK API
 */
public abstract class OkRequest<T> {

    private Odnoklassniki mOdnoklassniki;
    private Subscriber mSubscriber;

    public OkRequest(@NotNull Odnoklassniki ok) {
        mOdnoklassniki = ok;
    }

    abstract protected String getRequest(Odnoklassniki ok) throws IOException;

    public Observable<T> getObservable() {
        return prepareObservable();
    }

    private Observable<T> prepareObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mSubscriber = subscriber;
                String res = Utils.EMPTY;
                try {
                    res = getRequest(mOdnoklassniki);
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

    protected abstract Type getDataType();
}
