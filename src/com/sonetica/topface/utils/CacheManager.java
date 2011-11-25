package com.sonetica.topface.utils;

import java.util.Collection;
import java.util.HashMap;
import android.graphics.Bitmap;

/*
 *  Менеджер управляет скачанными изорбражениями.
 *  Хранение через слабые и мягкие ссылки
 */
public class CacheManager {
  // Data
  private HashMap<Integer,Bitmap> mCache;
  //---------------------------------------------------------------------------
  private CacheManager() {
    mCache = new HashMap<Integer,Bitmap>();
  }
  //---------------------------------------------------------------------------
  public static CacheManager getInstance() {
    return new CacheManager();
  }
  //---------------------------------------------------------------------------
  public void put(int key,Bitmap value) {
    mCache.put(key,value);
  }
  //---------------------------------------------------------------------------
  public Bitmap get(int key) {
    return mCache.get(key);
  }
  //---------------------------------------------------------------------------
  public boolean containsKey(int key) {
    return mCache.containsKey(key);
  }
  //---------------------------------------------------------------------------
  public void release() {
    Collection<Bitmap> values = mCache.values();
    for(Bitmap bitmap : values)
      bitmap.recycle();
    mCache.clear();
  }
  //---------------------------------------------------------------------------
}
