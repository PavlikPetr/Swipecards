package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.utils.Connectivity;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    public static enum ConnectionType {
        CONNECTION_OFFLINE(0),
        CONNECTION_MOBILE_3G(1),
        CONNECTION_MOBILE_EDGE(2),
        CONNECTION_WIFI(3);

        int type;

        ConnectionType(int i) {
            type = i;
        }

        private static Map<Integer, ConnectionType> map = new HashMap<Integer, ConnectionType>();

        static {
            for (ConnectionType connectionType : ConnectionType.values()) {
                map.put(connectionType.type, connectionType);
            }
        }

        public static ConnectionType valueOf(int type) {
            return map.get(type);
        }

        public int getInt() {
            return type;
        }

    }
    public static final String CONNECTION_TYPE = "connection_type";
    public static final String REAUTH = "reauth_after_internet_connected";
    private static ConnectionType mConnectionType;


    public boolean mIsConnected = false;
    private Context mContext;

    public ConnectionChangeReceiver(Context context) {
        super();
        mContext = context;
        updateConnectionStatus();
    }

    public static ConnectionType getConnectionType() {
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
                    mConnectionType = ConnectionType.CONNECTION_WIFI;
                    break;
                case THREE_G:
                    mConnectionType = ConnectionType.CONNECTION_MOBILE_3G;
                    break;
                case EDGE:
                    mConnectionType = ConnectionType.CONNECTION_MOBILE_EDGE;
                    break;
            }
        } else {
            mConnectionType = ConnectionType.CONNECTION_OFFLINE;
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

    public static interface OnConnectionChangedListener {
        public void onConnectionChanged(ConnectionType type);

    }
}