package com.topface.topface.data;

/**
 * используется для отложенной инициализации данных во фрагментах,
 * которые могут _внезапно_ получить данные, неуспев при этом создать свои view.
 * такое бывает у "вложенных" фрагментов, например по состоянию на 06.11.2014
 * UserProfileFragment имееет несколько вложенных, типа UserFormFragment, UserPhotoFragment etc.
 */
public class BasePendingInit<T> {
    protected T mData;
    protected boolean mCanSet;

    public BasePendingInit(T data, boolean canSet) {
        setData(data).setCanSet(canSet);
    }

    public BasePendingInit() {
        this(null, false);
    }

    public BasePendingInit setData(T data) {
        mData = data;
        return this;
    }

    public T getData() {
        return mData;
    }

    public BasePendingInit setCanSet(boolean canSet) {
        mCanSet = canSet;
        return this;
    }

    public boolean getCanSet() {
        return mCanSet && mData != null;
    }
}
