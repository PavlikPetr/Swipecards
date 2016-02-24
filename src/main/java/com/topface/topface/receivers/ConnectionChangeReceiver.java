package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.utils.Connectivity;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    public static enum ConnectionType {
        CONNECTION_OFFLINE(3),
        CONNECTION_MOBILE_3G(2),
        CONNECTION_MOBILE_EDGE(1),
        CONNECTION_WIFI(0);

        int type;

        ConnectionType(int i) {
            type = i;
        }

        private static SparseArray<ConnectionType> mConnectionMap = new SparseArray<>();

        static {
            for (ConnectionType connectionType : ConnectionType.values()) {
                mConnectionMap.put(connectionType.type, connectionType);
            }
        }

        public static ConnectionType valueOf(int type) {
            return mConnectionMap.get(type);
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
        mIsConnected = Connectivity.isConnected(mContext);
        Connectivity.Conn connectionType = Connectivity.getConnType(mContext);
        switch (connectionType) {
            case THREE_G:
                mConnectionType = ConnectionType.CONNECTION_MOBILE_3G;
                break;
            case EDGE:
                mConnectionType = ConnectionType.CONNECTION_MOBILE_EDGE;
                break;
            case OFF:
                mConnectionType = ConnectionType.CONNECTION_OFFLINE;
            case WIFI:
            case UNKNOWN:
            default:
                //Все неизвестные типы подключения считаем WiFi
                mConnectionType = ConnectionType.CONNECTION_WIFI;
        }
        sendBroadCastToActiveActivity();
        return connectionType;
    }

    private void reAuthIfNeed() {
        Intent intent = new Intent();
        intent.setAction(REAUTH);
        intent.putExtra(CONNECTION_TYPE, mConnectionType.getInt());
        mContext.sendBroadcast(intent);
    }

    private void sendToNavigation() {
        Intent intent = new Intent();
        intent.setAction(RetryRequestReceiver.RETRY_INTENT);
        intent.putExtra(CONNECTION_TYPE, mConnectionType.getInt());
        mContext.sendBroadcast(intent);
    }

    private void sendBroadCastToActiveActivity() {
        reAuthIfNeed();
        sendToNavigation();
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}
