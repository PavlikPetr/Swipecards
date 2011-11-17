package com.sonetica.topface.utils;

import java.util.HashMap;
import android.content.Context;
import android.graphics.Bitmap;

public class BitmapCache {
  // Data
  private Context mContext;
  private String  mFileCacheName;
  private HashMap<Integer,Bitmap> mCache = new HashMap<Integer,Bitmap>();
  //---------------------------------------------------------------------------
  public BitmapCache(Context context,String fileCacheName) {
    mContext = context;
    mFileCacheName = fileCacheName;
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
}
