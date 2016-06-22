package com.topface.topface.state;

/**
 * Created by petrp on 08.06.2016.
 * State of authorization
 */
public class AuthState extends MultiTypeDataObserver<DataAndBehaviorSubject> {

    public AuthState(CacheDataInterface listener) {
        super(listener);
    }

    @Override
    protected <T> DataAndBehaviorSubject generateData(T data) {
        return new DataAndBehaviorSubject(data);
    }
}
