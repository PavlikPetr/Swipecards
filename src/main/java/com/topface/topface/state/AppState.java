package com.topface.topface.state;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
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
        if (getCachebleData().containsKey(dataClass)) {
            getCachebleData().get(dataClass).setObject(data);
        } else {
            getCachebleData().put(dataClass, new DataAndObservable(data));
        }
        if (isNotifyListeners) {
            newEmmit(data, dataClass);
        }
    }

    public <T> Observable<T> getObservable(Class<T> dataClass) {
        if (!getCachebleData().containsKey(dataClass)) {
            T data = null;
            if (mCacheDataInterface != null) {
                data = mCacheDataInterface.getDataFromCache(dataClass);
            }
            setData(data, false, false, dataClass);
        }
        return getCachebleData().get(dataClass).getBehaviorSubject();
    }

    protected <T> T getNotNullData(@NotNull T data) {
        T res = (T) getData(data.getClass());
        return null == res ? data : res;
    }


    private <T> T getData(Class<T> dataClass) {
        if (getCachebleData().containsKey(dataClass)) {
            return (T) getCachebleData().get(dataClass).getObject();
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

    private ConcurrentHashMap<Class, DataAndObservable> getCachebleData() {
        if (mStateData == null) {
            mStateData = new ConcurrentHashMap<>();
        }
        return mStateData;
    }

    private <T> void newEmmit(T data, Class<?> dataClass) {
        if (getCachebleData().containsKey(dataClass)) {
            getCachebleData().get(dataClass).newEmmit(data);
        }
    }

    private <T> BehaviorSubject<T> getBehaviorSubjectNew(T data) {
        if (data == null) {
            return BehaviorSubject.create();
        } else {
            return BehaviorSubject.create(data);
        }
    }

    public class DataAndObservable<T> {
        private BehaviorSubject<T> behaviorSubject;
        private T object;

        public DataAndObservable(T data) {
            this(data, getBehaviorSubjectNew(data));
        }

        public DataAndObservable(T data, BehaviorSubject<T> behaviorSubject) {
            object = data;
            this.behaviorSubject = behaviorSubject;
        }

        public T getObject() {
            return object;
        }

        public void setObject(T object) {
            this.object = object;
        }

        public BehaviorSubject<T> getBehaviorSubject() {
            return behaviorSubject;
        }

        public void newEmmit(T data) {
            if (data != null) {
                getBehaviorSubject().onNext(data);
            }
        }
    }
}
