package com.topface.topface.state;

import rx.Observable;

/**
 * Created by ppavlik on 14.05.2016.
 * Base object with data and Observable
 */
public class DataAndObservable<DataType, ObservableType extends Observable<DataType>> {
    private ObservableType mObservable;
    private DataType mObject;

    protected ObservableType createObservable(DataType data) {
        return null;
    }

    protected void emmitData(DataType data) {

    }

    public DataAndObservable() {
    }

    public DataAndObservable(DataType data) {
        this();
        setObject(data);
        setObservable(createObservable(data));
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
