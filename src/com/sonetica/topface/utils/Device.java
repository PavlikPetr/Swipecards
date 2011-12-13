package com.sonetica.topface.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/*
 *  Менеджер работающий с железной частью устройства
 *  собирает и отдает параметры и характеристики:
 *  клавы,экрана,процессора,сенсоров,
 */
public class Device {
  //---------------------------------------------------------------------------
  public static Display getDisplay(Context context) {
    return ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
  }
  //---------------------------------------------------------------------------
  public static DisplayMetrics getDisplayMetrics(Context context) {
    Display display = getDisplay(context);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    display.getMetrics(displayMetrics);
    return displayMetrics;
  }
  //---------------------------------------------------------------------------
}
