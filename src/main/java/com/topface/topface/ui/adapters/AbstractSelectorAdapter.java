package com.topface.topface.ui.adapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.topface.topface.App;

/**
 * Abstract adapter for editing dialogs
 */
public abstract class AbstractSelectorAdapter<T> extends BaseAdapter {

    private LayoutInflater mInflater = LayoutInflater.from(App.getContext());

    public abstract T getData();

    protected View inflate(@LayoutRes int layoutId, ViewGroup parent) {
        return mInflater.inflate(layoutId, parent, false);
    }
}
