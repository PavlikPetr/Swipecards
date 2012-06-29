package com.topface.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;

public class CacheManager {
    // Data
    private StorageCache mStorageCache;
    private MemoryCache mMemoryCache;
    // Constants
    public static final int INTERNAL_CACHE = 0;
    public static final int EXTERNAL_CACHE = 1;
    //---------------------------------------------------------------------------
    public CacheManager(Context context) {
        this(context, EXTERNAL_CACHE);
    }
    //---------------------------------------------------------------------------
    public CacheManager(Context context,int cacheType) {
        mStorageCache = new StorageCache(context, cacheType);
        mMemoryCache = new MemoryCache();
    }
    //---------------------------------------------------------------------------
    public boolean containsKey(Integer position,String imageLink) {
        return true;
    }
    //---------------------------------------------------------------------------
    public Bitmap get(Integer position,String imageLink) {
        Bitmap bitmap = null;
        bitmap = mMemoryCache.get(position);
        if (bitmap != null)
            return bitmap;
        //bitmap = mStorageCache.load(Utils.md5(imageLink));
        //if(bitmap!=null)
        //mMemoryCache.put(position,bitmap);
        return bitmap;
    }
    //---------------------------------------------------------------------------
    public void put(Integer position,String imageLink,Bitmap bitmap) {
        mMemoryCache.put(position, bitmap);
        //mStorageCache.save(Utils.md5(imageLink),bitmap);
    }
    //---------------------------------------------------------------------------
    public void clear() {
        Debug.log(this, "clearing");
        mStorageCache.clear();
        mStorageCache = null;
        mMemoryCache.clear();
        mMemoryCache = null;
    }
    //---------------------------------------------------------------------------
}
