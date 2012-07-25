package com.topface.topface.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

public class MemoryCacheTemplate<K,V> {

	// Data
    private HashMap<K, SoftReference<V>> mCache;
    //---------------------------------------------------------------------------
    public MemoryCacheTemplate() {
        mCache = new HashMap<K, SoftReference<V>>();
    }
    //---------------------------------------------------------------------------
    public boolean containsKey(Integer key) {
        return mCache.containsKey(key);
    }
    //---------------------------------------------------------------------------
    public V get(K key) {
        SoftReference<V> ref = mCache.get(key);
        return ref != null ? ref.get() : null;
    }
    //---------------------------------------------------------------------------
    public void put(K key,V value) {
        mCache.put(key, new SoftReference<V>(value));
    }
    //---------------------------------------------------------------------------
    public void clear() {
        Debug.log(this, "memory cache clearing");        
        for (K key : mCache.keySet()) {
			V value = get(key);
			if (value != null) {
				if (value instanceof Bitmap) {
					((Bitmap) value).recycle();
				}
				mCache.put(key, null);
			}
		}        
        mCache.clear();
    }
    //--------------------------------------------------------------------------- 
	
}
