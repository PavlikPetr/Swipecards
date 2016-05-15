package com.topface.topface.state;

import org.jetbrains.annotations.NotNull;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by petrp on 14.05.2016.
 */
public class DataAndObserve<DataType> extends DataAndObservable<DataType, Observable<DataType>> {
    private Subscriber<? super DataType> mSubscriber;

    public DataAndObserve(DataType data) {
        super(data);
    }

    @Override
    @NotNull
    protected Observable<DataType> createObservable(DataType data) {
        return Observable.create(new Observable.OnSubscribe<DataType>() {
            @Override
            public void call(Subscriber<? super DataType> subscriber) {
                mSubscriber = subscriber;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void emmitData(@NotNull final DataType data) {
        if (mSubscriber != null) {
            mSubscriber.onNext(data);
        }
    }
}
