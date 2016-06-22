package com.topface.topface.state;

/**
 * Created by petrp on 14.05.2016.
 * Emmit fragments/activity lifecycle
 */
public class LifeCycleState extends MultiTypeDataObserver<DataAndSimpleObservable> {

    public LifeCycleState() {
        super(null);
    }

    @Override
    protected <T> DataAndSimpleObservable generateData(T data) {
        return new DataAndSimpleObservable(data);
    }
}
