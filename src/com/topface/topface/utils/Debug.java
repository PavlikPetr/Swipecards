package com.topface.topface.utils;

import com.topface.topface.App;
import android.util.Log;

public class Debug {
    //---------------------------------------------------------------------------
    public static void log(Object obj,String msg) {
        if (App.DEBUG) {
            if (obj == null)
                Log.i(App.TAG, "::" + msg);
            else if (obj instanceof String)
                Log.i(App.TAG, obj + "::" + msg);
            else
                Log.i(App.TAG, obj.getClass().getSimpleName() + "::" + msg);
        }
    }
    //---------------------------------------------------------------------------
}
