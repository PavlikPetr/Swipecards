package com.topface.topface.ui.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BillingService;
import com.topface.topface.billing.Consts;
import com.topface.topface.billing.PurchaseObserver;
import com.topface.topface.billing.ResponseHandler;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

public class BuyingFragment extends BaseFragment implements View.OnClickListener {

    private BillingService mBillingService;
    private RelativeLayout mBuy6;
    private RelativeLayout mBuy40;
    private RelativeLayout mBuy100;
    private RelativeLayout mBuy300;
    private RelativeLayout mVipBtn;
    private RelativeLayout mRecharge;

    private BroadcastReceiver mReceiver;

    public static final String BROADCAST_PURCHASE_ACTION = "com.topface.topface.PURCHASE_NOTIFICATION";
    private ServicesTextView mCurCoins;
    private ServicesTextView mCurPower;
    private TextView mResourcesInfo;

    public static BuyingFragment newInstance() {
        return new BuyingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_buy, null);

        mBillingService = new BillingService();
        mBillingService.setContext(getActivity());

        if (!mBillingService.checkBillingSupported(Consts.ITEM_TYPE_INAPP)) {
            Toast.makeText(getActivity().getApplicationContext(), "Play Market not available", Toast.LENGTH_SHORT)
                    .show();
        }

        initViews(root);

        ResponseHandler.register(new TopfacePurchaseObserver(new Handler()));
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
        mCurCoins = (ServicesTextView)root.findViewById(R.id.fbCurCoins);
        mCurPower = (ServicesTextView)root.findViewById(R.id.fbCurPower);
        mResourcesInfo = (TextView) root.findViewById(R.id.tvResourcesInfo);
        updateBalanceCounters();
    }

    private void updateBalanceCounters() {
        if(mCurCoins != null && mCurPower != null && mResourcesInfo != null) {
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

        mVipBtn = (RelativeLayout) root.findViewById(R.id.fbVipButton);

        if (CacheProfile.premium) {
            status.setText(getString(R.string.vip_state_on));
            vipBtnText.setText(R.string.vip_abilities);
            vipPrice.setVisibility(View.GONE);
        } else {
            status.setText(R.string.vip_state_off);
            vipBtnText.setText(R.string.vip_why);
            vipPrice.setVisibility(View.VISIBLE);
        }

        mVipBtn.setOnClickListener(new View.OnClickListener() {
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
        Intent intent = new Intent(getActivity(),ContainerActivity.class);
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
                mBillingService.requestPurchase("topface.coins.6", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.fb40Pack:
                mBillingService.requestPurchase("topface.coins.40", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.fb100Pack:
                mBillingService.requestPurchase("topface.coins.100", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.fb300Pack:
                mBillingService.requestPurchase("topface.coins.300", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.fbRecharge:
                mBillingService.requestPurchase("topface.energy.10000", Consts.ITEM_TYPE_INAPP, null);
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
                mBillingService.requestPurchase("android.test.purchased", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney40:
                mBillingService.requestPurchase("android.test.canceled", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney100:
                mBillingService.requestPurchase("android.test.refunded", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney300:
                mBillingService.requestPurchase("android.test.item_unavailable", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingPower:
                mBillingService.requestPurchase("android.test.purchased", Consts.ITEM_TYPE_INAPP, null);
                break;
        }
    }



    @Override
    public void onDestroy() {
        mBillingService.unbind();
        super.onDestroy();
    }

    private class TopfacePurchaseObserver extends PurchaseObserver {
        public TopfacePurchaseObserver(Handler handler) {
            super(getActivity(), handler);
        }

        @Override
        public void onBillingSupported(boolean supported, String type) {
            Debug.log("Buying: supported: " + supported);

            if (type == null || type.equals(Consts.ITEM_TYPE_INAPP)) {
                if (supported) {
                    mBuy6.setEnabled(true);
                    mBuy40.setEnabled(true);
                    mBuy100.setEnabled(true);
                    mBuy300.setEnabled(true);
                    mRecharge.setEnabled(true);
                } else {
                    mBuy6.setEnabled(false);
                    mBuy40.setEnabled(false);
                    mBuy100.setEnabled(false);
                    mBuy300.setEnabled(false);
                    mRecharge.setEnabled(false);
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.buy_play_market_not_available), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onPurchaseStateChange(Consts.PurchaseState purchaseState, String itemId,
                                          int quantity, long purchaseTime, String developerPayload,
                                          String signedData, String signature) {

            if (purchaseState == Consts.PurchaseState.PURCHASED) {
                Debug.log("Вот мы и купили, нужно понять что делать с интерфейсом");
            }
        }

        @Override
        public void onRequestPurchaseResponse(BillingService.RequestPurchase request,
                                              Consts.ResponseCode responseCode) {
            Debug.log("Buying " + request.mProductId + ": " + responseCode);
            if (responseCode == Consts.ResponseCode.RESULT_OK) {
                Debug.log("Buying: purchase was successfully sent to server");
            } else if (responseCode == Consts.ResponseCode.RESULT_USER_CANCELED) {
                Debug.log("Buying: user canceled purchase");
            } else {
                Debug.log("Buying: purchase failed");
            }
        }

        @Override
        public void onRestoreTransactionsResponse(BillingService.RestoreTransactions request,
                                                  Consts.ResponseCode responseCode) {
            if (responseCode == Consts.ResponseCode.RESULT_OK) {
                //Нам восстанавливать транзакции при переустановки приложения не нужно, нам об это и так сервер скажет
                Debug.log("Buying: completed RestoreTransactions request");
            } else {
                Debug.log("Buying: RestoreTransactions error: " + responseCode);
            }
        }
    }
}
