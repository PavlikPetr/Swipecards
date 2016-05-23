package com.topface.topface.state;

import rx.Observable;

/**
 * Created by ppavlik on 14.05.2016.
 * Base object with data and Observable
 */
class DataAndObservable<DataType, ObservableType extends Observable<DataType>> {
    private ObservableType mObservable;
    private DataType mObject;

    protected ObservableType createObservable(DataType data) {
        return null;
    }

    protected void emmitData(DataType data) {

    }

    DataAndObservable(DataType data) {
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

    void emmit(final DataType data) {
        emmitData(data);
    }
}
