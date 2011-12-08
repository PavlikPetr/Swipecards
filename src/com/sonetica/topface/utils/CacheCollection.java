package com.sonetica.topface.utils;

import java.util.HashMap;
import android.graphics.Bitmap;

abstract class AbstractCache {
  // Data
  protected CacheManager mCacheManager;
  protected HashMap<Integer,Bitmap> mData;
  protected IFrame mFrame;
  // Methods
  AbstractCache(CacheManager cacheManager) {
    mCacheManager = cacheManager;
  }
  public abstract boolean containsKey(int key);
  public abstract Bitmap get(int key);
  public abstract void put(int key,Bitmap value,String name);
  public abstract void release();
}
//---------------------------------------------------------------------------
//class TopsCache
//---------------------------------------------------------------------------
class TopsCache extends AbstractCache  {
  int width  = 120;
  int height = 140;
  TopsCache(CacheManager cacheManager) {
    super(cacheManager);
    mFrame = IFrame.TOPS;
    mData  = cacheManager.getData(mFrame);
  }
  public boolean containsKey(int key) {
    return mData.containsKey(key);
  }
  public Bitmap get(int key) {
    return mData.get(key);
  }
  public void put(int key,Bitmap value,String name) {
    if(value==null)
      return;
    // scaling
    Bitmap mScaledBitmap;
    if(width == value.getWidth() & height == value.getHeight())
      mScaledBitmap = value;
    else
      mScaledBitmap = Bitmap.createScaledBitmap(value, width, height, true /* filter */);
    
    mData.put(key,mScaledBitmap);
    //mCacheManager.save(mFrame,value,name);
  }
  public void release() {
//    for(Bitmap bitmap : mData.values())
//      if(bitmap!=null)
//        bitmap.recycle();
  }
}
//---------------------------------------------------------------------------
// class RateitCache
//---------------------------------------------------------------------------
class RateitCache extends AbstractCache  {
  RateitCache(CacheManager cacheManager) {
    super(cacheManager);
    mData  = cacheManager.getData(IFrame.TOPS);
  }
  public boolean containsKey(int key) {
    return mData.containsKey(key);
  }
  public Bitmap get(int key) {
    return mData.get(key);
  }
  public void put(int key,Bitmap value,String name) {
    mData.put(key,value);
  }
  public void release() {
    for(Bitmap bitmap : mData.values())
      if(bitmap!=null)
        bitmap.recycle();
  }
//---------------------------------------------------------------------------
}