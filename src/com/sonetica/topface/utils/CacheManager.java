package com.sonetica.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;

public class CacheManager {
  // Data
  private StorageCache mStorageCache;
  private MemoryCache  mMemoryCache;
  // Constants
  public static final int INTERNAL_CACHE = 0;
  public static final int EXTERNAL_CACHE = 1;
  //---------------------------------------------------------------------------
  public CacheManager(Context context,int cacheType) {
    mStorageCache = new StorageCache(context,cacheType);
    mMemoryCache  = new MemoryCache();
  }
  //---------------------------------------------------------------------------
  public boolean containsKey(Pair<Integer,String> data) {
    return true;
  }
  //---------------------------------------------------------------------------
  public Bitmap get(Pair<Integer,String> data) {
    return null;
  }
  //---------------------------------------------------------------------------
  public void put(Pair<Integer,String> data,Bitmap bitmap) {

  }
  //---------------------------------------------------------------------------
  public void clear() {
    mStorageCache.clear();
    mMemoryCache.clear();
    mStorageCache = null;
    mMemoryCache  = null;
  }
  //---------------------------------------------------------------------------
}
