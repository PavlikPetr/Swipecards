package com.topface.topface.utils;

import android.util.Log;
import com.topface.topface.App;

public class Debug {

    public static void log(Object obj, String msg) {
        if (App.DEBUG) {
            if (obj == null)
                Log.i(App.TAG, "::" + msg);
            else if (obj instanceof String)
                Log.i(App.TAG, obj + "::" + msg);
            else
                Log.i(App.TAG, obj.getClass().getSimpleName() + "::" + msg);
        }
    }

    public static void log(String msg) {
        if (App.DEBUG) {
            Log.i(App.TAG, msg);
        }
    }

    public static void error(String msg, Exception e) {
        if (App.DEBUG) {
            StringBuilder stack = new StringBuilder("\n");
            for (StackTraceElement st : e.getStackTrace()) {
                stack.append(st.toString()).append("\n");
            }
            msg = msg != null && !msg.equals("") ? msg + " : " : "";
            String errorText = e.getMessage() == null ? "" : " :: " + e.getMessage();
            Log.e(App.TAG, msg + errorText + stack.toString());
        }
    }

    public static void error(String msg) {
        if (App.DEBUG) {
            Log.e(App.TAG, msg);
        }
    }

    public static void error(Exception e) {
        error(null, e);
    }
}
