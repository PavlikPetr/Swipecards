package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.data.ViewLifreCycleData;
import com.topface.topface.state.LifeCycleState;

import javax.inject.Inject;


/**
 * Created by ppavlik on 13.05.16.
 * LifeCycle reporter for activities/fragments
 */
public class LifeCycleReporter<T extends ViewLifreCycleData> {

    @Inject
    LifeCycleState mLifeCycleState;

    public LifeCycleReporter() {
        App.get().inject(this);
    }

    public void emitLifeCycle(T data) {
        mLifeCycleState.setData(data);
    } 
}
