package com.topface.topface.state;

/**
 * Created by ppetr on 10.06.15.
 * interface for saving and data restore from cache
 */
public interface CacheDataInterface {
    <T> void saveDataToCache(T data);

    <T> T getDataFromCache(Class<T> classType);
}