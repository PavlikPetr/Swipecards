package com.topface.billing;

import android.os.Bundle;
import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Фрагмент, упрощающий создание фрагментов с покупками
 */
abstract public class BillingFragment extends BaseFragment implements BillingListener, BillingSupportListener {

    private BillingDriver mBillingDriver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBillingDriver = BillingDriverManager.getInstance().createMainBillingDriver(getActivity(), this, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBillingDriver.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBillingDriver.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBillingDriver.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBillingDriver.onDestroy();
    }

    protected BillingDriver getBillingDriver() {
        return mBillingDriver;
    }

    protected void buyItem(String itemId) {
        mBillingDriver.buyItem(itemId);
    }

    protected void buySubscriotion(String subscriptionId) {
        mBillingDriver.buySubscriotion(subscriptionId);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
