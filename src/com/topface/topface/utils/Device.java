package com.topface.topface.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class Device {
    // Data
    public static int width;
    // Constants
    public static final int W_240 = 240;
    public static final int W_320 = 320;
    public static final int W_480 = 480;
    public static final int W_540 = 540;
    public static final int W_600 = 600;
    public static final int W_720 = 720;
    public static final int W_800 = 800;
    // Orientation
    public static final int PORTRAIT = 0;
    public static final int LANDSCAPE = 1;
    public static final int WTF = 2;

    public static boolean init(Context context) {
        try {
            if (getOrientation(context) == LANDSCAPE)
                width = getDisplay(context).getHeight();
            else
                width = getDisplay(context).getWidth();

            if (width == 0) {
                Debug.log("Device", "init error");
                return false;
            }
        } catch (Exception e) {
            Debug.log("Device", "init exception:" + e);
            return false;
        }

        return true;
    }

    public static Display getDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    public static int getOrientation(Context context) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return PORTRAIT;
        }
        else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return LANDSCAPE;
        }

        return WTF;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        Display display = getDisplay(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        return displayMetrics;
    }

    public static int getCurrentDisplayWidth(Context context) {
        return getOrientation(context) == LANDSCAPE ?
                getDisplay(context).getHeight() :
                getDisplay(context).getWidth();
    }
}
