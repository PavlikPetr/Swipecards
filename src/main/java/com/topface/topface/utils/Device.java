package com.topface.topface.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.topface.topface.App;

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
            Point sizes = Utils.getSrceenSize(context);
            width = getOrientation(context) == LANDSCAPE ? sizes.y : sizes.x;

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
        } else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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

    /**
     * Возвращает максимальную сторону дисплея, т.е. ширину, если альбомная ориентация и высоту, если портретная
     *
     * @param context контекст выполнения
     * @return размер дисплея по максимальной стороне
     */
    public static int getMaxDisplaySize(Context context) {
        Point sizes = Utils.getSrceenSize(context);
        return getOrientation(context) == LANDSCAPE ?
                sizes.x : sizes.y;
    }

    public static int getMaxDisplaySize() {
        return getMaxDisplaySize(App.getContext());
    }
}
