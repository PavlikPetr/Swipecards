package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import com.topface.topface.RetryRequestReceiver;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    public boolean mIsConnected = false;
    private ConnectivityManager mConnectivityManager;
    private static int mConnectionType = 0;
    public static final int CONNECTION_OFFLINE = 0;
    public static final int CONNECTION_MOBILE = 1;
    public static final int CONNECTION_WIFI = 2;

    public static final String CONNECTION_TYPE = "connection_type";
    public static final String REAUTH = "reauth_after_internet_connected";
    Context ctx;

    public ConnectionChangeReceiver(Context context) {
        super();
        ctx = context;
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        updateConnectionStatus();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateConnectionStatus();
    }

    private void updateConnectionStatus() {
        NetworkInfo activeNetInfo = mConnectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            switch (activeNetInfo.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    //Toast.makeText(context, "Включена Мобильная сеть 3G/GPRS/EDGE", Toast.LENGTH_SHORT).show();
                    mConnectionType = CONNECTION_MOBILE;
                    break;
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_WIMAX:
                    //Toast.makeText(context, "Включен WIFI или WIMAX", Toast.LENGTH_SHORT).show();
                    mConnectionType = CONNECTION_WIFI;
                    break;
            }
            mIsConnected = activeNetInfo.isConnected();

//            reAuthIfNeed(ctx);
        } else {
//            Toast.makeText(context, "Интернет выключен", Toast.LENGTH_SHORT).show();
            mIsConnected = false;
            mConnectionType = CONNECTION_OFFLINE;
        }
        sendBroadCastToActiveActivity(ctx);
    }
    //TODO: Следующие два метода наверно можно объединить в один
    private void reAuthIfNeed() {
        Intent intent = new Intent();
        intent.setAction(REAUTH);
        intent.putExtra(CONNECTION_TYPE,mConnectionType);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

    private void sendToNavigation() {
        Intent intent = new Intent();
        intent.setAction(RetryRequestReceiver.RETRY_INTENT);
        intent.putExtra(CONNECTION_TYPE,mConnectionType);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

    private void sendBroadCastToActiveActivity(Context context) {
        reAuthIfNeed();
        sendToNavigation();
    }


    public boolean isConnected() {
        return mIsConnected;
    }

    public static int getConnectionType() {
        return mConnectionType;
    }

    public static boolean isMobileConnection() {
        return getConnectionType() == CONNECTION_MOBILE;
    }
}