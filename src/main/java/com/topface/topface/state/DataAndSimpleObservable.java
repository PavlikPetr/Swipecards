package com.topface.topface.state;

import com.topface.framework.utils.Debug;

import org.jetbrains.annotations.NotNull;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
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
        })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Debug.error("DataAndSimpleObservable subscribe");
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Debug.error("DataAndSimpleObservable failed");
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Debug.error("DataAndSimpleObservable unsubscribed");
                    }
                })
                .doOnNext(new Action1<DataType>() {
                    @Override
                    public void call(DataType dataType) {
                        Debug.error("DataAndSimpleObservable onNext " + dataType.toString());
                    }
                })
                .onBackpressureDrop().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).share();
    }

    @Override
    protected void emmitData(@NotNull final DataType data) {
        if (mSubscriber != null) {
            mSubscriber.onNext(data);
        }
    }
}
