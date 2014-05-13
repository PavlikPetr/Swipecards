package com.topface.statistics;

import com.nostra13.universalimageloader.core.ExtendedLoadAndDisplayImageTask;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.utils.Connectivity;

/**
 * Created by kirussell on 24.04.2014.
 *
 */
public class TfStatConsts {
    // statistics slices
    public static final String debug_val = "debug_val";
    // - connectivity
    public static final String val = "val";
    public static final String con = "con";
    public static final String mtd = "mtd";

    // statistics key name
    public static final String api_connect_time = "api_connect_time";
    public static final String api_load_time = "api_load_time";
    public static final String api_request_time = "api_request_time";

    public static String getConnType(Connectivity.Conn connectionType) {
        switch (connectionType) {
            case WIFI:
                return "wifi";
            case THREE_G:
                return "3g";
            case EDGE:
                return "edge";
            default:
                return "";
        }
    }

    public static String getConnTimeVal(long timeMillis) {
        if (timeMillis > 3000) {
            return "3000";
        } else if (timeMillis > 1000) {
            return "1000-3000";
        } else if (timeMillis > 600) {
            return "600-1000";
        } else if (timeMillis > 300) {
            return "300-600";
        } else if (timeMillis > 150) {
            return "150-300";
        } else if (timeMillis > 60) {
            return "60-150";
        } else if (timeMillis > 30) {
            return "30-60";
        } else {
            return "0-30";
        }
    }

    public static String getLoadTimeVal(long timeMillis) {
        if (timeMillis > 6000) {
            return "6000";
        } else if (timeMillis > 3000) {
            return "3000-6000";
        } else if (timeMillis > 1500) {
            return "1500-3000";
        } else if (timeMillis > 600) {
            return "600-1500";
        } else if (timeMillis > 300) {
            return "300-600";
        } else if (timeMillis > 100) {
            return "100-300";
        } else {
            return "0-100";
        }
    }

    public static String getRequestTimeVal(long timeMillis) {
        return getLoadTimeVal(timeMillis);
    }

    public static String getMtd(String serviceName) {
        if (serviceName == null) return "api";
        switch (serviceName) {
            case AuthRequest.SERVICE_NAME:
                return "auth";
            case PhotoAddRequest.SERVICE_NAME:
                return "imgUp";
            case ExtendedLoadAndDisplayImageTask.SERVICE_NAME:
                return "imgDwn";
            default:
                return "api";
        }
    }
}
