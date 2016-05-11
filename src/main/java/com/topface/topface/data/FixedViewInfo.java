package com.topface.topface.data;

import android.support.annotation.LayoutRes;
import android.view.View;

/**
 * Created by petrp on 10.05.2016.
 */
public class FixedViewInfo<T> {
    @LayoutRes
    private int mResId;
    private T mData;
    private OnViewClick<T> mClickListener;

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

    public void setData(T data) {
        mData = data;
    }

    public interface OnViewClick<T> {

        void onClick(View view, int itemPosition, T data);

    }
}
