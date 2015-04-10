package com.topface.topface.ui.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Abstract adapter for editing dialogs
 */
public abstract class AbstractEditAdapter<T> extends BaseAdapter {

    public interface OnDataChangeListener<T> {
        void onDataChanged(T data);
    }

    private LayoutInflater mInflater;
    private OnDataChangeListener<T> mDataChangeListener;

    public AbstractEditAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public abstract T getData();

    protected View inflate(ViewGroup parent) {
        return mInflater.inflate(getItemLayoutRes(), parent, false);
    }

    public abstract void saveData();

    @LayoutRes
    protected abstract int getItemLayoutRes();

    public void setDataChangeListener(OnDataChangeListener listener) {
        mDataChangeListener = listener;
    }

    public OnDataChangeListener<T> getDataChangeListener() {
        return mDataChangeListener;
    }
}
