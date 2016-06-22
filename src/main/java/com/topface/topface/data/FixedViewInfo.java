package com.topface.topface.data;

import android.support.annotation.LayoutRes;

/**
 * Created by petrp on 10.05.2016.
 * Base header/footer model for BaseHeaderFooterRecyclerViewAdapter
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

    @Override
    public int hashCode() {
        int res = mResId;
        return mResId * 31 + (mData != null ? mData.hashCode() : 0);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FixedViewInfo)) return false;
        FixedViewInfo data = (FixedViewInfo) o;
        if (mResId != data.getResId()) return false;
        return mData != null && mData.equals(data.getData());
    }
}
