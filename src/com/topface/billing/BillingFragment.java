package com.topface.billing;

import android.os.Bundle;
import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Фрагмент, упрощающий создание фрагментов с покупками
 */
abstract public class BillingFragment extends BaseFragment implements BillingListener, BillingSupportListener {

    public static final String ARG_TAG_SOURCE = "from_value";
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

    protected void buyItem(String itemId) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            BillingDriver.setSourceValue(arguments.getString(ARG_TAG_SOURCE, ""));
        }
        mBillingDriver.buyItem(itemId);
    }

    protected void buySubscription(String subscriptionId) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            BillingDriver.setSourceValue(getArguments().getString(ARG_TAG_SOURCE, ""));
        }
        mBillingDriver.buySubscription(subscriptionId);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
