package com.topface.topface.utils;

import com.topface.topface.data.ActivityLifreCycleData;

import static com.topface.topface.data.ActivityLifreCycleData.*;


/**
 * Created by ppavlik on 13.05.16.
 * LifeCycle reporter for activities/fragments
 */
public class ActivityLifeCycleReporter extends LifeCycleReporter<ActivityLifreCycleData> {
    private String mClassName;

    public ActivityLifeCycleReporter(String className) {
        mClassName = className;
    }

    private ActivityLifreCycleData getData(int state) {
        return new ActivityLifreCycleData(mClassName, state);
    }

    public void onDestroy() {
        emitLifeCycle(getData(DESTROYED));
    }

    public void onResume() {
        emitLifeCycle(getData(RESUMED));
    }

    public void onStop() {
        emitLifeCycle(getData(STOPPED));
    }

    public void onStart() {
        emitLifeCycle(getData(STARTED));
    }

    public void onSaveInstanceState() {
        emitLifeCycle(getData(SAVE_INSTANCE_STATE));
    }

    public void onCreate() {
        emitLifeCycle(getData(CREATED));
    }

    public void onPause() {
        emitLifeCycle(getData(PAUSED));
    }

    public void onRestart() {
        emitLifeCycle(getData(RESTARTED));
    }
}
