package com.sonetica.topface.utils;

import android.os.Debug;

public class Memory {
  //---------------------------------------------------------------------------  
  public static int getMaxHeap() {
    return (int)(Runtime.getRuntime().maxMemory())/1024;
  }
  //---------------------------------------------------------------------------
  public static int getHeapUsed() {
    Runtime runtime = Runtime.getRuntime();   
    return (int)(runtime.totalMemory()-runtime.freeMemory())/1024;
  }
  //---------------------------------------------------------------------------
  public static int getHeapFree() {  
    return (int)(Runtime.getRuntime().freeMemory())/1024;
  }
  //---------------------------------------------------------------------------
  public static int getNativeUsed() {
    return (int)(Debug.getNativeHeapSize()-Debug.getNativeHeapFreeSize())/1024;
  }
  //---------------------------------------------------------------------------
  public static int getNativeFree() {
    return (int)(Debug.getNativeHeapFreeSize())/1024;
  }
  //---------------------------------------------------------------------------
  public static int getUsedMemory() {
    return (int)(getHeapUsed() + getNativeUsed())/1024; 
  }
  //---------------------------------------------------------------------------
}
