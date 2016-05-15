package com.topface.topface.state;

/**
 * Created by petrp on 14.05.2016.
 */
public class LifeCycleState extends MultiTypeDataObserver<DataAndObserve> {

    public LifeCycleState() {
        super(null);
    }

    @Override
    protected <T> DataAndObserve generateData(T data) {
        return new DataAndObserve(data);
    }
}
