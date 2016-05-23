package com.topface.topface.state;

import org.jetbrains.annotations.NotNull;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ppavlik on 14.05.2016.
 * Object with data and Observable
 */
class DataAndSimpleObservable<DataType> extends DataAndObservable<DataType, Observable<DataType>> {
    private Subscriber<? super DataType> mSubscriber;

    DataAndSimpleObservable(DataType data) {
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
        }).onBackpressureDrop().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).share();
    }

    @Override
    protected void emmitData(@NotNull final DataType data) {
        if (mSubscriber != null) {
            mSubscriber.onNext(data);
        }
    }
}
