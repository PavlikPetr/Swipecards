package com.topface.topface.utils;

import com.topface.topface.App;
import android.util.Log;

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
            Log.e(App.TAG, msg + " :: " + e.getMessage() + stack.toString());
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
