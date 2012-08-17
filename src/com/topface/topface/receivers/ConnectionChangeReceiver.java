package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    public boolean mIsConnected = false;
    private ConnectivityManager mConnectivityManager;
    private int mConnectionType = CONNECTION_OFFLINE;
    public static final int CONNECTION_OFFLINE = 0;
    public static final int CONNECTION_MOBILE = 1;
    public static final int CONNECTION_WIFI = 2;

    public ConnectionChangeReceiver(Context context) {
        super();
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        updateConnectionStatus(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateConnectionStatus(context);
    }

    private void updateConnectionStatus(Context context) {
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
        }
        else {
            //Toast.makeText(context, "Интернет выключен", Toast.LENGTH_SHORT).show();
            mIsConnected = false;
            mConnectionType = CONNECTION_OFFLINE;
        }
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public int getConnectionType() {
        return mConnectionType;
    }
}