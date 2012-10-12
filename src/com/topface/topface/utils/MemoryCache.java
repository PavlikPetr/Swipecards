package com.topface.topface.utils;

import android.graphics.Bitmap;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/*
 *  Класс для хранения Битмапов с помощью слабых ссылок 
 */
public class MemoryCache {
    // Data
    private HashMap<Integer, WeakReference<Bitmap>> mCache;

    public MemoryCache() {
        mCache = new HashMap<Integer, WeakReference<Bitmap>>();
    }

    public boolean containsKey(Integer key) {
        return mCache.containsKey(key);
    }

    public Bitmap get(Integer position) {
        WeakReference<Bitmap> ref = mCache.get(position);
        return ref != null ? ref.get() : null;
    }

    public void put(Integer key, Bitmap bitmap) {
        mCache.put(key, new WeakReference<Bitmap>(bitmap));
    }

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

}
