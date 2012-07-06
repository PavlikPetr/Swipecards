package com.topface.topface.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class PixelsUtils {

	public static int convertPixelsToDp(float px,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    int dp =(int) (px / (metrics.densityDpi / 160f));
	    return dp;

	}

	public static int convertDpToPixel(float dp,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    int px = (int) (dp * (metrics.densityDpi/160f));
	    return px;
	}
}
