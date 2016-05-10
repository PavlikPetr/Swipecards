package com.topface.topface.data;

import android.support.annotation.LayoutRes;

/**
 * Created by petrp on 10.05.2016.
 */
public class FixedViewInfo<T> {
    @LayoutRes
    private int mResId;
    private T mData;

    public FixedViewInfo(@LayoutRes int resId, T data) {
        mResId = resId;
        mData = data;
    }

    @LayoutRes
    public int getResId() {
        return mResId;
    }

    public T getData() {
        return mData;
    }
}
