package com.topface.topface.state;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by ppetr on 10.06.15.
 * hold application state here
 */
public class AppState {
    private ConcurrentHashMap<Class, DataAndObservable> mStateData;
    private CacheDataInterface mCacheDataInterface;

    public AppState(CacheDataInterface listener) {
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
                getCachableData().put(dataClass, new DataAndObservable(data));
            }
        }
        if (isNotifyListeners) {
            emmit(data, dataClass);
        }
    }

    public <T> Observable<T> getObservable(Class<T> dataClass) {
        synchronized (getCachableData()) {
            if (!getCachableData().containsKey(dataClass)) {
                T data = null;
                if (mCacheDataInterface != null) {
                    data = mCacheDataInterface.getDataFromCache(dataClass);
                }
                setData(data, false, false, dataClass);
            }
            return getCachableData().get(dataClass).getBehaviorSubject();
        }
    }

    protected <T> T getNotNullData(@NotNull T defaultData) {
        T res = (T) getData(defaultData.getClass());
        return null == res ? defaultData : res;
    }

    public <T> boolean isEqualData(Class<T> dataClass, T data) {
        return getData(dataClass).equals(data);
    }

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
                return (T) res;
            }
        }
    }

    private ConcurrentHashMap<Class, DataAndObservable> getCachableData() {
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

    private <T> BehaviorSubject<T> createBehaviorSubject(T data) {
        if (data == null) {
            return BehaviorSubject.create();
        } else {
            return BehaviorSubject.create(data);
        }
    }

    private class DataAndObservable<T> {
        private BehaviorSubject<T> mBehaviorSubject;
        private T mObject;

        public DataAndObservable(T data) {
            this(data, createBehaviorSubject(data));
        }

        public DataAndObservable(T data, BehaviorSubject<T> behaviorSubject) {
            mObject = data;
            mBehaviorSubject = behaviorSubject;
            mBehaviorSubject.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        }

        public T getObject() {
            return mObject;
        }

        public void setObject(T object) {
            this.mObject = object;
        }

        public BehaviorSubject<T> getBehaviorSubject() {
            return mBehaviorSubject;
        }

        public void emmit(T data) {
            if (data != null) {
                getBehaviorSubject().onNext(data);
            }
        }
    }
}
