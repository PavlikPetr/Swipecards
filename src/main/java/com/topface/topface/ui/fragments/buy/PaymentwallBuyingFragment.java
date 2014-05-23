package com.topface.topface.ui.fragments.buy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Toast;

import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.utils.CacheProfile;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Абстрактный фрагмент с покупками, в котором есть кнопки покупки через Paymentwall
 */
public abstract class PaymentwallBuyingFragment extends AbstractBuyingFragment {
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PaymentwallActivity.ACTION_BUY) {
            if (resultCode == PaymentwallActivity.RESULT_OK) {
                //Когда покупка через Paymentwall завершена, показываем об этом сообщение
                Toast.makeText(getActivity(), R.string.buy_mobile_payments_complete, Toast.LENGTH_LONG).show();
                //И через 3 секунды обновляем профиль
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        App.sendProfileRequest();
                    }
                }, 3000);
            }
        }
    }

    @Override
    public void onInAppBillingUnsupported() {
        View paymentsTitle = getView().findViewById(R.id.titleMobilePayments);
        if (paymentsTitle != null) {
            paymentsTitle.setVisibility(View.GONE);
        }
    }

    protected void initPaymentwallButtons(View root) {
        //Показываем кнопку только на платформе Google Play v2
        String paymentwallLink = CacheProfile.getOptions().getPaymentwallLink();
        //Если сервер не прислал ссылку PW, то не показываем кнопки
        if (!TextUtils.isEmpty(paymentwallLink)) {
            //Paymentwall
            View mobilePayments = ((ViewStub) root.findViewById(R.id.mobilePayments)).inflate();
            ViewGroup layout = (ViewGroup) mobilePayments.findViewById(R.id.mobilePaymentsList);
            //Листенер просто открывае
            View.OnClickListener mobilePaymentsListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.startActivityForResult(
                                PaymentwallActivity.getIntent(
                                        activity,
                                        isTestPurchasesEnabled()
                                ),
                                PaymentwallActivity.ACTION_BUY
                        );
                    }
                }
            };

            //На все кнопки навешиваем листенер нажатия
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child != null) {
                    child.setOnClickListener(mobilePaymentsListener);
                }
            }
        }
    }

    @Override
    protected void buy(Products.BuyButton btn) {
        super.buy(btn);
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.startActivityForResult(
                    PaymentwallActivity.getIntent(
                            activity,
                            isTestPurchasesEnabled()
                    ),
                    PaymentwallActivity.ACTION_BUY
            );
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPaymentwallButtons(getView());
    }
}
