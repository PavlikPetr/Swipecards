package com.topface.billing;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.topface.topface.R;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.Utils;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //После того как View создано проверяем, нужно ли показывать переключатель тестовых покупок
        if (mBillingDriver.isTestPurchasesAvailable()) {
            ViewStub stub = (ViewStub) getView().findViewById(R.id.EditorTestStub);
            if (stub != null) {
                View layout = stub.inflate();
                //Инициализируем красивый переключатель
                final EditSwitcher checkBox = new EditSwitcher(
                        (ViewGroup) layout.findViewById(R.id.EditorTestBuyCheckBox),
                        getResources().getString(R.string.editor_test_buy)
                );
                //Выставляем значение по умолчанию
                checkBox.setChecked(mBillingDriver.isTestPurchasesEnabled());

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BillingDriver.setTestPaymentsState(
                                checkBox.doSwitch()
                        );
                    }
                });
            }
        }
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
}
