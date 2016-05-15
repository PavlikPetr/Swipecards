package com.topface.topface.state;

import org.jetbrains.annotations.NotNull;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by petrp on 14.05.2016.
 */
public class DataAndObservable<DataType, ObservableType extends Observable<DataType>> {
    private ObservableType mObservable;
    private DataType mObject;

    protected ObservableType createObservable(DataType data){
        return null;
    }

    protected void emmitData(DataType data){

    }

    public DataAndObservable() {
    }

    public DataAndObservable(DataType data) {
        this();
        setObject(data);
        setObservable(createObservable(data));
    }

    private DataAndObservable(DataType data, ObservableType observable) {
        this();
        setObject(data);
        setObservable(observable);
    }

    public DataType getObject() {
        return mObject;
    }

    public void setObject(DataType object) {
        this.mObject = object;
    }

    public ObservableType getObservable() {
        return mObservable;
    }

    private void setObservable(ObservableType observable) {
        mObservable = observable;
    }

    public void emmit(final DataType data) {
        emmitData(data);
    }
}
