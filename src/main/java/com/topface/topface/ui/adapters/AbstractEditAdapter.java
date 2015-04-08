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

    private LayoutInflater mInflater;

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
}
