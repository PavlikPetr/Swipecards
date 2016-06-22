package com.topface.topface.state;

public class EventBus extends MultiTypeDataObserver<DataAndSimpleObservable> {

    public EventBus() {
        super(null);
    }

    @Override
    protected <T> DataAndSimpleObservable generateData(T data) {
        return new DataAndSimpleObservable(data);
    }
}
