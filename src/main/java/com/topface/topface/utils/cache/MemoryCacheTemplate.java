package com.topface.topface.utils.cache;

import android.graphics.Bitmap;

import com.topface.framework.utils.Debug;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class MemoryCacheTemplate<K, V> {

    // Data
    private HashMap<K, SoftReference<V>> mCache;


    public MemoryCacheTemplate() {
        mCache = new HashMap<>();
    }


    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean containsKey(Integer key) {
        return mCache.containsKey(key);
    }


    public V get(K key) {
        SoftReference<V> ref = mCache.get(key);
        return ref != null ? ref.get() : null;
    }


    public void put(K key, V value) {
        mCache.put(key, new SoftReference<>(value));
    }


    public void clear() {
        Debug.log(this, "memory cache clearing");
        for (K key : mCache.keySet()) {
            V value = get(key);
            if (value != null) {
                if (value instanceof Bitmap) {
                    if (!((Bitmap) value).isRecycled()) {
                        ((Bitmap) value).recycle();
                    } else {
                        Debug.error("Bitmap is already recycled");
                    }
                }
                mCache.put(key, null);
            }
        }
        mCache.clear();
    }


}
