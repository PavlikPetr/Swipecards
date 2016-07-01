package com.topface.topface.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by kirussell on 24.04.2014.
 */
public class Connectivity {

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    private static boolean isNetworkTypeWifi(int type) {
        switch (type) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_ETHERNET:
                return true;
            default:
                return false;
        }
    }

    private static boolean isNetworkTypeMobile(int type) {
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_MMS:
            case ConnectivityManager.TYPE_MOBILE_SUPL:
            case ConnectivityManager.TYPE_MOBILE_DUN:
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_BLUETOOTH:
                return true;
            default:
                return false;
        }
    }

    public static boolean isWifiConnected(Context context) {
        return getConnType(context) == Conn.WIFI;
    }

    public static Conn getConnType(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        if (info != null && info.isConnected()) {
            int type = info.getType();
            if (isNetworkTypeWifi(type)) {
                return Conn.WIFI;
            } else if (isNetworkTypeMobile(type)) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:// ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11 ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9 ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13 ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11 ~ 10+ Mbps
                        return Conn.THREE_G;
                    case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:  // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8 ~ 25 kbps
                        return Conn.EDGE;
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:
                        if (type == ConnectivityManager.TYPE_WIMAX) {
                            return Conn.THREE_G;
                        }
                        return Conn.UNKNOWN;
                }
            } else {
                return Conn.UNKNOWN;
            }
        } else {
            return Conn.OFF;
        }
    }

    public static enum Conn {WIFI, EDGE, THREE_G, OFF, UNKNOWN}
}
