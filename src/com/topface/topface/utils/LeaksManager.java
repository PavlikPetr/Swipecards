package com.topface.topface.utils;

import java.lang.ref.WeakReference;
import java.util.Vector;

/*
 *   Класс для сбора активити, которые не удалились после закрытия
 */
public class LeaksManager {
  // Data
  private static LeaksManager mInstance;
  private final Vector<WeakReference<Object>> mRefsList;
  //---------------------------------------------------------------------------
  private LeaksManager() {
      mRefsList = new Vector<WeakReference<Object>>();
  }
  //---------------------------------------------------------------------------
  public static LeaksManager getInstance() {
    if(mInstance == null)
      mInstance = new LeaksManager();
    return mInstance;
  }
  //---------------------------------------------------------------------------
  public <T> T monitorObject(T obj) {
    if(obj == null)
      return obj;
    
    /*  прповерка на дублирование
    for(WeakReference<Object> ref : mRefsList)
      if(ref.get() == obj)
        return obj;
    */
    
    mRefsList.add(new WeakReference<Object>(obj));
     
    return obj;
  }
  //---------------------------------------------------------------------------
  public Vector<String> checkLeaks() {
    //System.gc();
     
    Vector<String> frames = new Vector<String>();
     
    for(int i=mRefsList.size()-1;i>=0;i--) {
      WeakReference<Object> ref = mRefsList.elementAt(i);
      Object obj = ref.get();
      if(obj != null) {
        String className = obj.getClass().getSimpleName();
        frames.add(className);
        //addUniqueClassName(frames, TextUtils.isEmpty(className) ? "Unknown class name" : className);
      } else
        mRefsList.remove(i);
    }

    mRefsList.trimToSize();
     
    return frames;
  }
  //---------------------------------------------------------------------------
  /*
  private void addUniqueClassName(Vector<String> frames, String className) {
    int index = -1;
    for(int j=0;j<frames.size(); j++) {
      if(frames.elementAt(j).equals(className)) {
        index = j;
        break;
      }
    }
    if(index == -1)
      frames.add(frames.getClass().getSimpleName());
  }
  */
  //---------------------------------------------------------------------------
}
