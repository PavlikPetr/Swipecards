package com.sonetica.topface.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.graphics.Bitmap;

/*
 *  Класс для хранения Битмапов с помощью слабых ссылок 
 */
public class MemoryCache {
  // Data
  private HashMap<Integer,SoftReference<Bitmap>> mCache;
  //---------------------------------------------------------------------------
  public MemoryCache() {
    mCache = new HashMap<Integer, SoftReference<Bitmap>>();
  }
  //---------------------------------------------------------------------------
  public Bitmap get(Integer position){
    SoftReference<Bitmap> ref = mCache.get(position);
    return ref != null ? ref.get() : null;
  }
  //---------------------------------------------------------------------------
  public void put(Integer key, Bitmap bitmap){
    mCache.put(key,new SoftReference<Bitmap>(bitmap));
  }
  //---------------------------------------------------------------------------
  public void clear() {
    Debug.log(this,"clearing");
    mCache.clear();
  }
  //---------------------------------------------------------------------------  
}
