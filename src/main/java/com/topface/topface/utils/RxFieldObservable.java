package com.topface.topface.utils;

import android.databinding.ObservableField;

import org.jetbrains.annotations.Nullable;

import rx.Observable;
import rx.Subscriber;

/**
 * rx обертка для ObservableField
 * Created by tiberal on 28.06.16.
 */
public class RxFieldObservable<T> extends ObservableField<T> {

    private Observable<T> mFiledObservable;
    private Subscriber<? super T> mFieldSubscriber;

    public RxFieldObservable(@Nullable T value) {
        super(value);
        createObservable();
    }

    public RxFieldObservable() {
        createObservable();
    }

    private void createObservable() {
        mFiledObservable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                mFieldSubscriber = subscriber;
            }
        });
    }

    public void setIgnoreEmit(T value) {
        super.set(value);
    }

    @Override
    public void set(T value) {
        emitValue(value);
        super.set(value);
    }

    private void emitValue(T value) {
        if (mFieldSubscriber != null) {
            mFieldSubscriber.onNext(value);
        }
    }

    public Observable<T> getFiledObservable() {
        return mFiledObservable;
    }
}
