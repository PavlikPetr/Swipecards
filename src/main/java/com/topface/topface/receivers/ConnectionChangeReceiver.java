package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.utils.Connectivity;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    public static final int CONNECTION_OFFLINE = 0;
    public static final int CONNECTION_MOBILE = 1;
    public static final int CONNECTION_WIFI = 2;
    public static final String CONNECTION_TYPE = "connection_type";
    public static final String REAUTH = "reauth_after_internet_connected";
    private static int mConnectionType = 0;
    public boolean mIsConnected = false;
    private Context mContext;

    public ConnectionChangeReceiver(Context context) {
        super();
        mContext = context;
        updateConnectionStatus();
    }

    public static int getConnectionType() {
        return mConnectionType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Connectivity.Conn connectionType = updateConnectionStatus();
        StatisticsTracker.getInstance()
                .setConfiguration(App.getAppOptions().getStatisticsConfiguration(mIsConnected, connectionType));
    }

    private Connectivity.Conn updateConnectionStatus() {
        Connectivity.Conn connectionType = Connectivity.Conn.UNKNOWN;
        mIsConnected = Connectivity.isConnected(mContext);
        if (mIsConnected) {
            connectionType = Connectivity.getConnType(mContext);
            switch (connectionType) {
                case WIFI:
                    mConnectionType = CONNECTION_WIFI;
                    break;
                case THREE_G:
                case EDGE:
                    mConnectionType = CONNECTION_MOBILE;
                    break;
            }
        } else {
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
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void sendToNavigation() {
        Intent intent = new Intent();
        intent.setAction(RetryRequestReceiver.RETRY_INTENT);
        intent.putExtra(CONNECTION_TYPE, mConnectionType);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void sendBroadCastToActiveActivity() {
        reAuthIfNeed();
        sendToNavigation();
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}