package com.topface.topface.ui;

import com.topface.topface.utils.PurchasesEvents;

/**
 * Created by ppetr on 20.07.15.
 * empty parrent for NavigationActivity (it needs in i-free flavour)
 */
public abstract class ParentNavigationActivity extends BaseFragmentActivity {
    @Override
    protected int getContentLayout() {
        return getContentLayoutId();
    }

    protected abstract int getContentLayoutId();

    @Override
    protected void onProfileUpdated() {
        super.onProfileUpdated();
        new PurchasesEvents().checkRenewSubscription(this.getApplicationContext());
    }
}