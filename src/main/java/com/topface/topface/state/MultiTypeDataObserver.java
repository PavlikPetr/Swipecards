package com.topface.topface.state;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by ppetr on 10.06.15.
 * base data by class name observable
 */
public abstract class MultiTypeDataObserver<ObserveDataType extends DataAndObservable> {
    private ConcurrentHashMap<Class, ObserveDataType> mStateData;
    private CacheDataInterface mCacheDataInterface;

    public MultiTypeDataObserver(CacheDataInterface listener) {
        mCacheDataInterface = listener;
    }

    public <T> void setData(T data) {
        if (null != data) {
            setData(data, true, true, data.getClass());
        }
    }

    private <T> void setData(T data, boolean isSaveCache, boolean isNotifyListeners) {
        setData(data, isSaveCache, isNotifyListeners, data.getClass());
    }

    private <T> void setData(T data, boolean isSaveCache, boolean isNotifyListeners, Class<?> dataClass) {
        if (isSaveCache && mCacheDataInterface != null) {
            mCacheDataInterface.saveDataToCache(data);
        }
        synchronized (getCachableData()) {
            if (getCachableData().containsKey(dataClass)) {
                getCachableData().get(dataClass).setObject(data);
            } else {
                getCachableData().put(dataClass, generateData(data));
            }
        }
        if (isNotifyListeners) {
            emmit(data, dataClass);
        }
    }

    @Nullable
    protected <T> ObserveDataType generateData(T data) {
        return null;
    }

    public <T> void destroyObservable(Class<T> dataClass) {
        if (getCachableData().contains(dataClass)) {
            getCachableData().remove(dataClass);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Observable<T> getObservable(Class<T> dataClass) {
        synchronized (getCachableData()) {
            if (!getCachableData().containsKey(dataClass)) {
                T data = null;
                if (mCacheDataInterface != null) {
                    data = mCacheDataInterface.getDataFromCache(dataClass);
                }
                setData(data, false, false, dataClass);
            }
            return getCachableData().get(dataClass).getObservable().doOnError(new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    @NotNull
    protected <T> T getNotNullData(@NotNull T defaultData) {
        T res = (T) getData(defaultData.getClass());
        return null == res ? defaultData : res;
    }

    public <T> boolean isEqualData(Class<T> dataClass, T data) {
        return getData(dataClass).equals(data);
    }

    @Nullable
    private <T> T getData(Class<T> dataClass) {
        synchronized (getCachableData()) {
            if (getCachableData().containsKey(dataClass)) {
                return (T) getCachableData().get(dataClass).getObject();
            } else {
                T res = null;
                if (mCacheDataInterface != null) {
                    res = mCacheDataInterface.getDataFromCache(dataClass);
                    if (res != null) {
                        setData(res, false, true);
                    } else {
                        setData(res, false, true, dataClass);
                    }
                }
                return res;
            }
        }
    }

    private ConcurrentHashMap<Class, ObserveDataType> getCachableData() {
        if (mStateData == null) {
            mStateData = new ConcurrentHashMap<>();
        }
        return mStateData;
    }

    private <T> void emmit(T data, Class<?> dataClass) {
        synchronized (getCachableData()) {
            if (getCachableData().containsKey(dataClass)) {
                getCachableData().get(dataClass).emmit(data);
            }
        }
    }
}
