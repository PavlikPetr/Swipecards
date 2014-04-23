package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.RetryRequestReceiver;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    public static final int CONNECTION_OFFLINE = 0;
    public static final int CONNECTION_MOBILE = 1;
    public static final int CONNECTION_WIFI = 2;
    public static final String CONNECTION_TYPE = "connection_type";
    public static final String REAUTH = "reauth_after_internet_connected";
    private static int mConnectionType = 0;
    public boolean mIsConnected = false;
    Context ctx;
    private ConnectivityManager mConnectivityManager;

    public ConnectionChangeReceiver(Context context) {
        super();
        ctx = context;
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        updateConnectionStatus();
    }

    public static int getConnectionType() {
        return mConnectionType;
    }

    public static boolean isMobileConnection() {
        return getConnectionType() == CONNECTION_MOBILE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int connectionType = updateConnectionStatus();
        StatisticsTracker.getInstance()
                .setConfiguration(App.getAppOptions().getStatisticsConfiguration(mIsConnected, connectionType));
    }

    private int updateConnectionStatus() {
        NetworkInfo activeNetInfo = mConnectivityManager.getActiveNetworkInfo();
        int connectionType = -1;
        if (activeNetInfo != null) {
            connectionType = activeNetInfo.getType();
            switch (connectionType) {
                case ConnectivityManager.TYPE_MOBILE:
                    mConnectionType = CONNECTION_MOBILE;
                    break;
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_WIMAX:
                    mConnectionType = CONNECTION_WIFI;
                    break;
            }
            mIsConnected = activeNetInfo.isConnected();
        } else {
            mIsConnected = false;
            mConnectionType = CONNECTION_OFFLINE;
        }
        sendBroadCastToActiveActivity();
        return connectionType;
    }

    //TODO: Следующие два метода наверно можно объединить в один
    private void reAuthIfNeed() {
        Intent intent = new Intent();
        intent.setAction(REAUTH);
        intent.putExtra(CONNECTION_TYPE, mConnectionType);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

    private void sendToNavigation() {
        Intent intent = new Intent();
        intent.setAction(RetryRequestReceiver.RETRY_INTENT);
        intent.putExtra(CONNECTION_TYPE, mConnectionType);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

    private void sendBroadCastToActiveActivity() {
        reAuthIfNeed();
        sendToNavigation();
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}