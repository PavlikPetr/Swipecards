package com.topface.topface.ui.adapters.test;

import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Контейнер для вьюх, которые нужно воткнуть в лист.
 * Created by tiberal on 23.06.16.
 */
public class InjectViewBucket {

    public static final int VIEW = 0;
    public static final int RES = 1;
    public static final int FACTORY = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIEW, RES, FACTORY})
    @interface MetamorphoseType {
    }

    @Nullable
    private IViewInjectRule mFilter;
    @LayoutRes
    private int mMetamorphoseRes;
    @Nullable
    private View mInjectView;
    @Nullable
    private IInjectViewFactory mFactory;
    @MetamorphoseType
    private int mType;

    public InjectViewBucket(@LayoutRes int metamorphoseRes) {
        mMetamorphoseRes = metamorphoseRes;
        mType = RES;
    }

    public InjectViewBucket(@NotNull View metamorphoseView) {
        mInjectView = metamorphoseView;
        mType = VIEW;
    }

    public InjectViewBucket(@NotNull IInjectViewFactory factory) {
        mFactory = factory;
        mType = FACTORY;
    }

    public void addFilter(@NotNull IViewInjectRule filter) {
        mFilter = filter;
    }

    @Nullable
    public IViewInjectRule getFilter() {
        return mFilter;
    }

    @LayoutRes
    public int getInjectLayoutRes() {
        return mMetamorphoseRes;
    }

    @Nullable
    public View getInjectLayout() {
        return mInjectView;
    }

    @Nullable
    public IInjectViewFactory getInjectLayoutFactory() {
        return mFactory;
    }

    public int getType() {
        return mType;
    }

}
