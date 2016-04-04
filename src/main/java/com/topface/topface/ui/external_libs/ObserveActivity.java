package com.topface.topface.ui.external_libs;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.topface.topface.data.ActivityLifreCycleData;

import org.jetbrains.annotations.NotNull;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

import static com.topface.topface.data.ActivityLifreCycleData.ActivityLifecycle.*;

/**
 * Created by ppavlik on 04.04.16.
 * Observe activity lifecycle
 */
public class ObserveActivity extends ActionBarActivity {

    private static Subscriber<? super ActivityLifreCycleData> mLifeCycleSubscriber;
    private static Observable<ActivityLifreCycleData> mActivityLifecycleObservable = Observable.create(new Observable.OnSubscribe<ActivityLifreCycleData>() {
        @Override
        public void call(Subscriber<? super ActivityLifreCycleData> subscriber) {
            mLifeCycleSubscriber = subscriber;
        }
    }).doOnError(new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
        }
    });

    @NotNull
    public static Observable<ActivityLifreCycleData> getLifeCycleObservable() {
        return mActivityLifecycleObservable;
    }

    private void emitLifeCycle(ActivityLifreCycleData.ActivityLifecycle lifecycle) {
        if (mLifeCycleSubscriber != null && !mLifeCycleSubscriber.isUnsubscribed()) {
            mLifeCycleSubscriber.onNext(new ActivityLifreCycleData(getLocalClassName(), lifecycle));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        emitLifeCycle(DESTROYED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        emitLifeCycle(RESUMED);
    }

    @Override
    public void onStop() {
        super.onStop();
        emitLifeCycle(STOPPED);
    }

    @Override
    public void onStart() {
        super.onStart();
        emitLifeCycle(STARTED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        emitLifeCycle(SAVE_INSTANCE_STATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        emitLifeCycle(CREATED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        emitLifeCycle(PAUSED);
    }
}
