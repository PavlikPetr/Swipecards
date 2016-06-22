package com.topface.topface.state;

/**
 * Created by petrp on 14.05.2016.
 * Cacheble app state
 */
public class AppState extends MultiTypeDataObserver<DataAndBehaviorSubject> {

    public AppState(CacheDataInterface listener) {
        super(listener);
    }

    @Override
    protected <T> DataAndBehaviorSubject generateData(T data) {
        return new DataAndBehaviorSubject(data);
    }
}
