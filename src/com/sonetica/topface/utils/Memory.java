package com.sonetica.topface.utils;

import android.os.Debug;

public class Memory {
  //---------------------------------------------------------------------------  
  public static long getMaxHeap() {
    return Runtime.getRuntime().maxMemory();
  }
  //---------------------------------------------------------------------------
  public static long getUsedHeap() {
    Runtime runtime = Runtime.getRuntime();   
    return runtime.totalMemory()-runtime.freeMemory();
  }
  //---------------------------------------------------------------------------
  public static long getUsedNative() {
    return Debug.getNativeHeapSize()-Debug.getNativeHeapFreeSize();
  }
  //---------------------------------------------------------------------------
  public static long getUsedMemory() {
    return getUsedHeap() + getUsedNative(); 
  }
  //---------------------------------------------------------------------------
}
