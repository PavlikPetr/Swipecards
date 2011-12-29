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
  public boolean containsKey(Integer key) {
    return mCache.containsKey(key);
  }
  //---------------------------------------------------------------------------
  public Bitmap get(Integer key){
    if(!mCache.containsKey(key))
      return null;
    SoftReference<Bitmap> ref = mCache.get(key);
    return ref.get();
  }
  //---------------------------------------------------------------------------
  public void put(Integer key, Bitmap bitmap){
    mCache.put(key,new SoftReference<Bitmap>(bitmap));
  }
  //---------------------------------------------------------------------------
  public void clear() {
    mCache.clear();
  }
  //---------------------------------------------------------------------------  
}
