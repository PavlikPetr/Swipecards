package com.topface.topface.ui;

/**
 * Created by ppetr on 20.07.15.
 * empty parrent for NavigationActivity (it needs in i-free flavour)
 */
public abstract class ParrentNavigationActivity extends BaseFragmentActivity {
    @Override
    protected int getContentLayout() {
        return getContentLayoutId();
    }

    protected abstract int getContentLayoutId();
}