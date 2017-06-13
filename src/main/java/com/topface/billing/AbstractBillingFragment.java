package com.topface.billing;

import com.topface.topface.App;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;

/**
 * Абстрактный фрагмент без реализации платежей
 */
public abstract class AbstractBillingFragment extends BaseFragment implements IBilling {
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
        return mIsTestPurchasesAvailable || (!CacheProfile.isEmpty() && App.get().getProfile().isEditor());
    }

    /**
     * Включен ли режим тестовых покупок
     */
    public boolean isTestPurchasesEnabled() {
        //На всякий случай проверяем, что доступны тестовые платежи
        return isTestPurchasesAvailable() && mIsTestPayments;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
