package com.topface.topface.utils;

import com.topface.statistics.TfStatConsts;
import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Created by kirussell on 28.04.2014.
 * Listener for connection process
 */
public class RequestConnectionListener {

    private final StatisticsTracker mTracker;
    private final Slices mSlices;
    private long mConnStartedTime;
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

    public void onConnectionEstablished() {
        mConnEstablishedTime = System.currentTimeMillis();
        mTracker.sendEvent(
                TfStatConsts.api_connect_time,
                mSlices.putSlice(TfStatConsts.val, TfStatConsts.getConnTimeVal(mConnEstablishedTime - mConnStartedTime))
        );
    }

    public void onConnectionClose() {
        long connClosedTime = System.currentTimeMillis();
        mTracker.sendEvent(
                TfStatConsts.api_load_time,
                mSlices.putSlice(TfStatConsts.val, TfStatConsts.getConnTimeVal(connClosedTime - mConnEstablishedTime))
        );
        mTracker.sendEvent(
                TfStatConsts.api_request_time,
                mSlices.putSlice(TfStatConsts.val, TfStatConsts.getRequestTimeVal(connClosedTime - mConnStartedTime))
        );
    }
}
