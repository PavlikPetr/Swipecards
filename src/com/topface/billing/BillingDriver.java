package com.topface.billing;

import android.app.Activity;

abstract public class BillingDriver {
    BillingListener mBillingListener;
    private final Activity mActivity;
    protected BillingSupportListener mBillingSupportListener;

    public BillingDriver(Activity activity, BillingListener listener) {
        mActivity = activity;
        setBillingListener(listener);
    }

    /**
     * Этот метод нужно вызвать в активити покупки для отработки всех событий
     */
    public abstract void onDestroy();

    public abstract void buyItem(String itemId);

    public abstract void buySubscriotion(String subscriptionId);

    protected void setBillingListener(BillingListener listener) {
        mBillingListener = listener;
    }

    public BillingListener getBillingListener() {
        return mBillingListener;
    }

    public abstract String getDriverName();

    protected Activity getActivity() {
        return mActivity;
    }

    public void setBillingSupportListener(BillingSupportListener listener) {
        mBillingSupportListener = listener;
        checkBillingSupport(mBillingSupportListener);
    }

    protected abstract void checkBillingSupport(BillingSupportListener billingSupportListener);
}
