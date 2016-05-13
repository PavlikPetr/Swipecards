package com.topface.topface.utils;

import com.topface.topface.data.ViewLifreCycleData1;

import org.jetbrains.annotations.NotNull;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by ppavlik on 13.05.16.
 * LifeCycle reporter for activities/fragments
 */
public class LifeCycleReporter<T extends ViewLifreCycleData1> {
    private Subscriber<? super T> mLifeCycleSubscriber;
    private Observable<T> mActivityLifecycleObservable;

    public LifeCycleReporter() {
        mActivityLifecycleObservable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                mLifeCycleSubscriber = subscriber;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).share();
    }

    @NotNull
    public Observable<T> getLifeCycleObservable() {
        return mActivityLifecycleObservable;
    }

    public void emitLifeCycle(T data) {
        if (mLifeCycleSubscriber != null && !mLifeCycleSubscriber.isUnsubscribed()) {
            mLifeCycleSubscriber.onNext(data);
        }
    }
}
