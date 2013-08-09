package com.topface.billing;

import android.app.Activity;

/**
 * Универсальный драйвер покупки через внешнее API.
 * На его основе пишутся остальные драйверы и работа с ними идет прозрачно через интерфейс BillingDriver
 */
abstract public class BillingDriver {
    private static String mSource;
    private BillingListener mBillingListener;
    private final Activity mActivity;
    protected BillingSupportListener mBillingSupportListener;

    public BillingDriver(Activity activity, BillingListener listener) {
        mActivity = activity;
        setBillingListener(listener);
    }

    public abstract void onStart();

    public abstract void onResume();

    public abstract void onStop();

    public abstract void onDestroy();

    public abstract void buyItem(String itemId);

    public abstract void buySubscription(String subscriptionId);

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
    }

    public BillingSupportListener getBillingSupportListener() {
        return mBillingSupportListener;
    }

    /**
     * Так как у нас сложная структура покупки, совместо с очередью не удается переслать дополнительные данные при покупки
     * Поэтому просто используем статичный метод при покупке, что бы отправить доп данные на сервер
     */
    public static void setSourceValue(String source) {
        mSource = source;
    }

    public static String getSourceValue() {
        return mSource;
    }
}
