package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.topface.topface.utils.ActivityLifeCycleReporter;
import com.topface.topface.utils.IActivityDelegate;

/**
 * Created by ppavlik on 04.04.16.
 * Observe activity lifecycle
 */
public class TrackedLifeCycleActivity extends AppCompatActivity implements IActivityDelegate {

    private ActivityLifeCycleReporter mLifeCycleReporter = new ActivityLifeCycleReporter(getClass().getName());
    private boolean mIsActivityRestoredState = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLifeCycleReporter.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLifeCycleReporter.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLifeCycleReporter.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifeCycleReporter.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLifeCycleReporter.onSaveInstanceState();
        mIsActivityRestoredState = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifeCycleReporter.onCreate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLifeCycleReporter.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mLifeCycleReporter.onRestart();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mIsActivityRestoredState = true;
    }

    public boolean isActivityRestoredState() {
        return mIsActivityRestoredState;
    }

}
