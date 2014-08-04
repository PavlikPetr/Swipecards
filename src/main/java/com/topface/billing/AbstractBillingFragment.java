package com.topface.billing;

import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;

/**
 * Абстрактный фрагмент без реализации платежей
 */
public abstract class AbstractBillingFragment extends BaseFragment {
    protected static final String IS_TEST_PURCHASES_AVAILABLE = "IS_TEST_PURCHASES_AVAILABLE";
    protected static final String IS_TEST_PURCHASES_ENABLED = "IS_TEST_PURCHASES_ENABLED";
    protected static boolean mIsTestPayments = false;
    protected boolean mIsTestPurchasesAvailable = false;

    public static void setTestPaymentsState(boolean testPaymentsState) {
        mIsTestPayments = testPaymentsState;
    }

    /**
     * Доступны ли тестовые платежи
     */
    public boolean isTestPurchasesAvailable() {
        return mIsTestPurchasesAvailable || (!CacheProfile.isEmpty() && CacheProfile.isEditor());
    }

    /**
     * Включен ли режим тестовых покупок
     */
    public boolean isTestPurchasesEnabled() {
        //На всякий случай проверяем, что доступны тестовые платежи
        return isTestPurchasesAvailable() && mIsTestPayments;
    }

    @SuppressWarnings("UnusedDeclaration")
    abstract public void onPurchased(final String productId);

    abstract public void onSubscriptionSupported();

    abstract public void onSubscriptionUnsupported();

    abstract public void onInAppBillingSupported();

    abstract public void onInAppBillingUnsupported();

    @Override
    public boolean isTrackable() {
        return false;
    }
}
