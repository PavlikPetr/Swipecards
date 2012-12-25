package com.topface.topface.ui.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.billing.BillingDriver;
import com.topface.billing.BillingDriverManager;
import com.topface.billing.BillingListener;
import com.topface.billing.BillingSupportListener;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;

public class BuyingFragment extends BaseFragment implements View.OnClickListener, BillingSupportListener {

    private RelativeLayout mBuy6;
    private RelativeLayout mBuy40;
    private RelativeLayout mBuy100;
    private RelativeLayout mBuy300;
    private RelativeLayout mRecharge;

    private BroadcastReceiver mReceiver;

    public static final String BROADCAST_PURCHASE_ACTION = "com.topface.topface.PURCHASE_NOTIFICATION";
    private ServicesTextView mCurCoins;
    private ServicesTextView mCurPower;
    private TextView mResourcesInfo;
    private BillingDriver mBillindDriver;

    public static BuyingFragment newInstance() {
        return new BuyingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_buy, null);
        initViews(root);

        mBillindDriver = BillingDriverManager.getInstance().createMainBillingDriver(getActivity(), new BillingListener() {
            @Override
            public void onPurchased() {
                updateBalanceCounters();
            }

            @Override
            public void onError() {
                //TODO: Сделать обработку ошибок
            }

            @Override
            public void onCancel() {
                //Возможно стоит добавить реакцию на отмену покупки пользователем
            }
        }, this);


        return root;
    }

    private void initViews(View root) {
        initBalanceCounters(root);
        initButtons(root);
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBalanceCounters();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void initBalanceCounters(View root) {
        mCurCoins = (ServicesTextView) root.findViewById(R.id.fbCurCoins);
        mCurPower = (ServicesTextView) root.findViewById(R.id.fbCurPower);
        mResourcesInfo = (TextView) root.findViewById(R.id.tvResourcesInfo);
        updateBalanceCounters();
    }

    private void updateBalanceCounters() {
        if (mCurCoins != null && mCurPower != null && mResourcesInfo != null) {
            mCurCoins.setText(Integer.toString(CacheProfile.money));
            mCurPower.setText(Integer.toString(CacheProfile.power));
            if (CacheProfile.money > 0) {
                mResourcesInfo.setText(getResources().getString(R.string.buying_default_message));
            } else {
                mResourcesInfo.setText(String.format(
                        getResources().getString(R.string.buying_you_have_no_coins_for_gift), CacheProfile.money));
            }
        }
    }

    private void initButtons(View root) {
        mBuy6 = (RelativeLayout) root.findViewById(R.id.fb6Pack);
        mBuy6.setOnClickListener(this);

        mBuy40 = (RelativeLayout) root.findViewById(R.id.fb40Pack);
        mBuy40.setOnClickListener(this);

        mBuy100 = (RelativeLayout) root.findViewById(R.id.fb100Pack);
        mBuy100.setOnClickListener(this);

        mBuy300 = (RelativeLayout) root.findViewById(R.id.fb300Pack);
        mBuy300.setOnClickListener(this);

        mRecharge = (RelativeLayout) root.findViewById(R.id.fbRecharge);
        mRecharge.setOnClickListener(this);

        TextView status = (TextView) root.findViewById(R.id.vip_status);
        TextView vipBtnText = (TextView) root.findViewById(R.id.fbVipBtnText);
        TextView vipPrice = (TextView) root.findViewById(R.id.vipPrice);

        RelativeLayout vipBtn = (RelativeLayout) root.findViewById(R.id.fbVipButton);

        if (CacheProfile.premium) {
            status.setText(getString(R.string.vip_state_on));
            vipBtnText.setText(R.string.vip_abilities);
            vipPrice.setVisibility(View.GONE);
        } else {
            status.setText(R.string.vip_state_off);
            vipBtnText.setText(R.string.vip_why);
            vipPrice.setVisibility(View.VISIBLE);
        }

        vipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToVipSettings();
            }
        });

    }

    private void goToVipSettings() {
//        if(getActivity() != null) {
//            ((ContainerActivity)getActivity()).startFragment(ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
//        }
        Intent intent = new Intent(getActivity(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        requestPurchase(view);
    }

    private void requestPurchase(View view) {
        switch (view.getId()) {
            case R.id.fb6Pack:
                mBillindDriver.buyItem("topface.coins.6");
                break;
            case R.id.fb40Pack:
                mBillindDriver.buyItem("topface.coins.40");
                break;
            case R.id.fb100Pack:
                mBillindDriver.buyItem("topface.coins.100");
                break;
            case R.id.fb300Pack:
                mBillindDriver.buyItem("topface.coins.300");
                break;
            case R.id.fbRecharge:
                mBillindDriver.buyItem("topface.energy.10000");
                break;
        }
    }

    /**
     * Тестовые товары для отладки покупок
     * NOTE: Применяется только для тестирования!
     *
     * @param view кнопка покупки
     */
    private void requestTestPurchase(View view) {
        switch (view.getId()) {
            case R.id.btnBuyingMoney6:
                mBillindDriver.buyItem("android.test.purchased");
                break;
            case R.id.btnBuyingMoney40:
                mBillindDriver.buyItem("android.test.canceled");
                break;
            case R.id.btnBuyingMoney100:
                mBillindDriver.buyItem("android.test.refunded");
                break;
            case R.id.btnBuyingMoney300:
                mBillindDriver.buyItem("android.test.item_unavailable");
                break;
            case R.id.btnBuyingPower:
                mBillindDriver.buyItem("android.test.purchased");
                break;
        }
    }


    @Override
    public void onDestroy() {
        mBillindDriver.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onInAppBillingSupported() {
        mBuy6.setEnabled(true);
        mBuy40.setEnabled(true);
        mBuy100.setEnabled(true);
        mBuy300.setEnabled(true);
        mRecharge.setEnabled(true);
    }

    @Override
    public void onSubscritionSupported() {
        //TODO: добавить поддержку подписок
    }

    @Override
    public void onInAppBillingUnsupported() {
        mBuy6.setEnabled(false);
        mBuy40.setEnabled(false);
        mBuy100.setEnabled(false);
        mBuy300.setEnabled(false);
        mRecharge.setEnabled(false);
        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.buy_play_market_not_available), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscritionUnsupported() {
        //TODO: добавить поддержку подписок
    }

}
