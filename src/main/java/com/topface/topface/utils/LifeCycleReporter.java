package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.data.ViewLifreCycleData1;
import com.topface.topface.state.LifeCycleState;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by ppavlik on 13.05.16.
 * LifeCycle reporter for activities/fragments
 */
public class LifeCycleReporter<T extends ViewLifreCycleData1> {

    @Inject
    LifeCycleState mLifeCycleState;

    public LifeCycleReporter() {
        App.get().inject(this);
    }

    public void emitLifeCycle(T data) {
        mLifeCycleState.setData(data);
    } 
}
