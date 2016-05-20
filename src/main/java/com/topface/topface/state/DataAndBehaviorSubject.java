package com.topface.topface.state;

import org.jetbrains.annotations.NotNull;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subjects.BehaviorSubject;

/**
 * Created by ppavlik on 14.05.2016.
 * Object with data and BehaviorSubject
 */
public class DataAndBehaviorSubject<DataType> extends DataAndObservable<DataType, BehaviorSubject<DataType>> {

    public DataAndBehaviorSubject(DataType dataType) {
        super(dataType);
    }

    @Override
    @NotNull
    protected BehaviorSubject<DataType> createObservable(DataType data) {
        if (data == null) {
            return BehaviorSubject.create();
        } else {
            return BehaviorSubject.create(data);
        }
    }

    @Override
    protected void emmitData(final DataType data) {
        if (data != null) {
            AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    getObservable().onNext(data);
                }
            });
        }
    }
}
