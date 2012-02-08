package com.sonetica.topface.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.graphics.Bitmap;

/*
 *  Класс для хранения Битмапов с помощью слабых ссылок 
 */
public class MemoryCacheEx {
  // Data
  private HashMap<String,SoftReference<Bitmap>> mCache;
  //---------------------------------------------------------------------------
  public MemoryCacheEx() {
    mCache = new HashMap<String, SoftReference<Bitmap>>();
  }
  //---------------------------------------------------------------------------
  public Bitmap get(String position){
    SoftReference<Bitmap> ref = mCache.get(position);
    return ref != null ? ref.get() : null;
  }
  //---------------------------------------------------------------------------
  public void put(String key, Bitmap bitmap){
    mCache.put(key,new SoftReference<Bitmap>(bitmap));
  }
  //---------------------------------------------------------------------------
  public void clear() {
    Debug.log(this,"clearing");
    mCache.clear();
  }
  //---------------------------------------------------------------------------  
}
