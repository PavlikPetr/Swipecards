package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.data.ViewLifreCycleData;
import com.topface.topface.state.LifeCycleState;

/**
 * Created by ppavlik on 13.05.16.
 * LifeCycle reporter for activities/fragments
 */
public class LifeCycleReporter<T extends ViewLifreCycleData> {

    private LifeCycleState mLifeCycleState;

    public LifeCycleReporter() {
        mLifeCycleState = App.getAppComponent().lifeCycleState();
    }

    public void emitLifeCycle(T data) {
        mLifeCycleState.setData(data);
    }
}
