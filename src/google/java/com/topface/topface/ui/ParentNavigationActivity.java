package com.topface.topface.ui;

import android.databinding.ViewDataBinding;

/**
 * Created by ppetr on 20.07.15.
 * empty parrent for NavigationActivity (it needs in i-free flavour)
 */
public abstract class ParentNavigationActivity<T extends ViewDataBinding> extends BaseFragmentActivity<T> {

    protected abstract int getContentLayoutId();
}