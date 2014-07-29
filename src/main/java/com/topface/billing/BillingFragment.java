package com.topface.billing;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.Utils;

/**
 * Фрагмент, упрощающий создание фрагментов с покупками
 */
abstract public class BillingFragment extends BaseFragment implements BillingListener, BillingSupportListener {

    private static final String IS_EDITOR = "IS_EDITOR";
    private static final String IS_TEST_PURCHASES_ENABLED = "IS_TEST_PURCHASES_ENABLED";

    public static final String ARG_TAG_SOURCE = "from_value";
    private BillingDriver mBillingDriver;
    private boolean mIsTestPurchasesEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBillingDriver = getBillingDriver();
    }

    protected abstract BillingDriver getBillingDriver();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //После того как View создано проверяем, нужно ли показывать переключатель тестовых покупок

        boolean isEditor = false;
        boolean isTestPurchases = false;
        if (savedInstanceState != null) {
            isEditor = savedInstanceState.getBoolean(IS_EDITOR, false);
            isTestPurchases = savedInstanceState.getBoolean(IS_TEST_PURCHASES_ENABLED, false);
        }

        if (mBillingDriver.isTestPurchasesAvailable() || isEditor) {
            ViewStub stub = (ViewStub) getView().findViewById(R.id.EditorTestStub);
            if (stub != null) {
                View layout = stub.inflate();
                //Инициализируем красивый переключатель
                final EditSwitcher checkBox = new EditSwitcher(
                        (ViewGroup) layout.findViewById(R.id.EditorTestBuyCheckBox),
                        getResources().getString(R.string.editor_test_buy)
                );
                //Выставляем значение по умолчанию
                if (isTestPurchases) {
                    BillingDriver.setTestPaymentsState(true);
                }
                checkBox.setChecked(mBillingDriver.isTestPurchasesEnabled() || isTestPurchases);

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BillingDriver.setTestPaymentsState(
                                checkBox.doSwitch()
                        );
                        mIsTestPurchasesEnabled = checkBox.isChecked();
                    }
                });
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_EDITOR, mBillingDriver.isTestPurchasesAvailable());
        outState.putBoolean(IS_TEST_PURCHASES_ENABLED, mIsTestPurchasesEnabled);
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

    /**
     * Chooses buyItem or buySubscription based on btn class type
     * (@link(Products.SubscriptionBuyButton) or Products.BuyButton)
     *
     * @param btn BuyButton object to determine which type of buy is processing
     */
    protected void buy(Products.BuyButton btn) {
        String itemId = btn.id;
        if (itemId != null) {
            if (btn.type.isSubscription()) {
                buySubscription(btn.id);
            } else {
                buyItem(btn.id);
            }
        }
    }

    protected void buyItem(String itemId) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            BillingDriver.setSourceValue(arguments.getString(ARG_TAG_SOURCE));
        }
        mBillingDriver.buyItem(itemId);
    }

    protected void buySubscription(String subscriptionId) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            BillingDriver.setSourceValue(getArguments().getString(ARG_TAG_SOURCE));
        }
        mBillingDriver.buySubscription(subscriptionId);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    protected void editSubscriptions() {
        Utils.goToMarket(getActivity());
    }

    protected boolean isTestPurchasesEnabled() {
        return mBillingDriver.isTestPurchasesEnabled();
    }

}
