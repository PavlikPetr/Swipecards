package com.topface.topface.utils;

import com.topface.statistics.TfStatConsts;
import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;

/**
 * Created by kirussell on 28.04.2014.
 * Listener for connection process
 */
public class RequestConnectionListener {

    private final StatisticsTracker mTracker;
    private final Slices mSlices;
    private long mConnStartedTime;
    private long mConnInvokedTime;
    private long mConnEstablishedTime;

    public RequestConnectionListener(String serviceName) {
        mTracker = StatisticsTracker.getInstance();
        mSlices = new Slices()
                .putSlice(TfStatConsts.con, TfStatConsts.getConnType(Connectivity.getConnType(App.getContext())))
                .putSlice(TfStatConsts.mtd, TfStatConsts.getMtd(serviceName));
    }

    public void onConnectionStarted() {
        mConnStartedTime = System.currentTimeMillis();
    }

    public void onConnectInvoked() {
        mConnInvokedTime = System.currentTimeMillis();
    }

    public void onConnectionEstablished() {
        mConnEstablishedTime = System.currentTimeMillis();
        long interval = mConnEstablishedTime - mConnInvokedTime;
        addDebugVal(interval);
        mTracker.sendEvent(
                TfStatConsts.api_connect_time,
                mSlices.putSlice(TfStatConsts.val, getConnTimeVal(interval))
        );
    }

    public void onConnectionClose() {
        long connClosedTime = System.currentTimeMillis();
        long interval = connClosedTime - mConnEstablishedTime;
        addDebugVal(interval);
        mTracker.sendEvent(
                TfStatConsts.api_load_time,
                mSlices.putSlice(TfStatConsts.val, getConnTimeVal(interval))
        );
        interval = connClosedTime - mConnStartedTime;
        addDebugVal(interval);
        mTracker.sendEvent(
                TfStatConsts.api_request_time,
                mSlices.putSlice(TfStatConsts.val, getRequestTimeVal(interval))
        );
    }

    private void addDebugVal(long val) {
        if (BuildConfig.DEBUG) {
            mSlices.put(TfStatConsts.debug_val, Long.toString(val));
        }
    }

    protected String getConnTimeVal(long interval) {
        return TfStatConsts.getConnTimeVal(interval);
    }

    protected String getRequestTimeVal(long interval) {
        return TfStatConsts.getRequestTimeVal(interval);
    }
}
