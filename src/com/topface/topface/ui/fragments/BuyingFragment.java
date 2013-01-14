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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.billing.BillingFragment;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;

import java.util.LinkedList;

public class BuyingFragment extends BillingFragment implements View.OnClickListener {

    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_GIFT = 1;
    public static final int TYPE_DELIGHT = 2;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";

    private LinkedList<RelativeLayout> purchaseButtons = new LinkedList<RelativeLayout>();

    private BroadcastReceiver mReceiver;

    public static final String BROADCAST_PURCHASE_ACTION = "com.topface.topface.PURCHASE_NOTIFICATION";
    private ServicesTextView mCurCoins;
    private ServicesTextView mCurLikes;
    private TextView mResourcesInfo;

    public static BuyingFragment newInstance(int type, int coins) {
        BuyingFragment fragment = new BuyingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_TYPE, type);
        args.putInt(ARG_ITEM_PRICE, coins);
        fragment.setArguments(args);
        return fragment;
    }

    public static BuyingFragment newInstance() {
        return new BuyingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_buy, null);
        initViews(root);
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
        mCurLikes = (ServicesTextView) root.findViewById(R.id.fbCurLikes);
        mResourcesInfo = (TextView) root.findViewById(R.id.tvResourcesInfo);
        updateBalanceCounters();
    }

    private void updateBalanceCounters() {
        if (mCurCoins != null && mCurLikes != null && mResourcesInfo != null) {
            mCurCoins.setText(Integer.toString(CacheProfile.money));
            mCurLikes.setText(Integer.toString(CacheProfile.likes));

            Bundle args = getArguments();
            if (args != null) {
                int type = args.getInt(ARG_ITEM_TYPE);
                int coins = args.getInt(ARG_ITEM_PRICE);
                switch (type) {
                    case TYPE_GIFT:
                        mResourcesInfo.setText(String.format(
                                getResources().getString(R.string.buying_you_have_no_coins_for_gift),
                                coins - CacheProfile.money));
                        break;
                    default:
                        mResourcesInfo.setText(getResources().getString(R.string.buying_default_message));
                        break;
                }
            } else {
                mResourcesInfo.setText(getResources().getString(R.string.buying_default_message));
            }

        }
    }

    private void initButtons(View root) {
        LinearLayout likesButtons = (LinearLayout) root.findViewById(R.id.fbLikes);
        for (Options.BuyButton curButton: CacheProfile.getOptions().likes) {
            RelativeLayout newButton = Options.setButton(likesButtons, curButton, getActivity(), new Options.BuyButtonClickListener() {
                @Override
                public void onClick(String id) {
                    buyItem(id);
                }
            });
            if(newButton != null) {
                purchaseButtons.add(newButton);
            }
        }


        LinearLayout coinsButtons = (LinearLayout) root.findViewById(R.id.fbCoins);
        for (Options.BuyButton curButton: CacheProfile.getOptions().coins) {
            RelativeLayout newButton = Options.setButton(coinsButtons, curButton, getActivity(), new Options.BuyButtonClickListener() {
                @Override
                public void onClick(String id) {
                    buyItem(id);
                }
            });
            if(newButton != null) {
                purchaseButtons.add(newButton);
            }
        }


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

        if (CacheProfile.getOptions().premium.isEmpty()) {
            status.setVisibility(View.GONE);
            vipBtnText.setVisibility(View.GONE);
            vipPrice.setVisibility(View.GONE);
        } else {
            status.setVisibility(View.VISIBLE);
            vipBtnText.setVisibility(View.VISIBLE);
            vipPrice.setVisibility(View.VISIBLE);
        }

    }

    private void goToVipSettings() {
        Intent intent = new Intent(getActivity(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        requestPurchase(view);
    }

    private void requestPurchase(View view) {
//        switch (view.getId()) {
//            case R.id.fb6Pack:
//                buyItem("topface.coins.6");
//                break;
//            case R.id.fb40Pack:
//                buyItem("topface.coins.40");
//                break;
//            case R.id.fb100Pack:
//                buyItem("topface.coins.100");
//                break;
//            case R.id.fb300Pack:
//                buyItem("topface.coins.300");
//                break;
//            case R.id.fbRecharge:
//                buyItem("topface.energy.10000");
//                break;
//        }
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
                buyItem("android.test.purchased");
                break;
            case R.id.btnBuyingMoney40:
                buyItem("android.test.canceled");
                break;
            case R.id.btnBuyingMoney100:
                buyItem("android.test.refunded");
                break;
            case R.id.btnBuyingMoney300:
                buyItem("android.test.item_unavailable");
                break;
            case R.id.btnBuyingLikes:
                buyItem("android.test.purchased");
                break;
        }
    }

    @Override
    public void onInAppBillingSupported() {
        for (RelativeLayout btn: purchaseButtons)  {
            btn.setEnabled(true);
        }
    }

    @Override
    public void onSubscritionSupported() {
        //TODO: добавить поддержку подписок
    }

    @Override
    public void onInAppBillingUnsupported() {
        for (RelativeLayout btn: purchaseButtons)  {
            btn.setEnabled(false);
        }
        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.buy_play_market_not_available), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscritionUnsupported() {
        //TODO: добавить поддержку подписок
    }

    @Override
    public void onPurchased() {
        updateBalanceCounters();
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
    }

    @Override
    public void onError() {
        //TODO: Сделать обработку ошибок
    }

    @Override
    public void onCancel() {
        //Возможно стоит добавить реакцию на отмену покупки пользователем
    }
}
