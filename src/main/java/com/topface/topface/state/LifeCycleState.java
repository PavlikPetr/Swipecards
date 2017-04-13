package com.topface.topface.state;

/**
 * Created by petrp on 14.05.2016.
 * Emmit fragments/activity lifecycle
 */
public class LifeCycleState extends MultiTypeDataObserver<NoBackpreasureObservable> {

    public LifeCycleState() {
        super(null);
    }

    @Override
    protected <T> NoBackpreasureObservable generateData(T data) {
        return new NoBackpreasureObservable(data);
    }
}
