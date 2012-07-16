package com.topface.topface.utils;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import android.graphics.Bitmap;

/* Класс для хранения Битмапов с помощью слабых ссылок */
public class MemorySyncCache {
    // Data
    private ConcurrentHashMap<Integer, SoftReference<Bitmap>> mCache;
    //---------------------------------------------------------------------------
    public MemorySyncCache() {
        mCache = new ConcurrentHashMap<Integer, SoftReference<Bitmap>>();
    }
    //---------------------------------------------------------------------------
    public boolean containsKey(Integer key) {
        return mCache.containsKey(key);
    }
    //---------------------------------------------------------------------------
    public Bitmap get(Integer position) {
        SoftReference<Bitmap> ref = mCache.get(position);
        return ref != null ? ref.get() : null;
    }
    //---------------------------------------------------------------------------
    public void put(Integer key,Bitmap bitmap) {
        mCache.put(key, new SoftReference<Bitmap>(bitmap));
    }
    //---------------------------------------------------------------------------
    public void clear() {
        Debug.log(this, "memory cache clearing");
        int size = mCache.size();
        for (int i = 0; i < size; i++) {
            Bitmap bitmap = get(i);
            if (bitmap != null) {
                bitmap.recycle();
                mCache.put(i, null); // хз
            }
        }
        mCache.clear();
    }
    //---------------------------------------------------------------------------  
}
