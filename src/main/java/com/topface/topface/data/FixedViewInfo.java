package com.topface.topface.data;

import android.support.annotation.LayoutRes;

/**
 * Created by petrp on 10.05.2016.
 */
public class FixedViewInfo<DataType> {
    @LayoutRes
    private int mResId;
    private DataType mData;

    public FixedViewInfo(@LayoutRes int resId, DataType data) {
        mResId = resId;
        mData = data;
    }

    @LayoutRes
    public int getResId() {
        return mResId;
    }

    public DataType getData() {
        return mData;
    }

    public void setData(DataType data) {
        mData = data;
    }
}
