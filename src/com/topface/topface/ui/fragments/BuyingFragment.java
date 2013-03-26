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
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Offerwalls;

import java.util.LinkedList;

@SuppressWarnings("UnusedDeclaration")
public class BuyingFragment extends BillingFragment {

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
        ActionBar actionBar = getActionBar(root);
        actionBar.showBackButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        actionBar.setTitleText(getString(R.string.buying_header_title));
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


        if (CacheProfile.getOptions().coins.isEmpty()) {
            root.findViewById(R.id.coins_title).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.coins_title).setVisibility(View.VISIBLE);
        }

        if (CacheProfile.getOptions().likes.isEmpty()) {
            root.findViewById(R.id.likes_title).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.likes_title).setVisibility(View.VISIBLE);
        }

        if (CacheProfile.getOptions().likes.isEmpty() && CacheProfile.getOptions().coins.isEmpty()) {
            root.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        }

        for (Options.BuyButton curButton : CacheProfile.getOptions().likes) {
            RelativeLayout newButton = Options.setButton(likesButtons, curButton, getActivity(), new Options.BuyButtonClickListener() {
                @Override
                public void onClick(String id) {
                    buyItem(id);
                }
            });
            if (newButton != null) {
                purchaseButtons.add(newButton);
            }
        }


        LinearLayout coinsButtons = (LinearLayout) root.findViewById(R.id.fbCoins);
        for (Options.BuyButton curButton : CacheProfile.getOptions().coins) {
            RelativeLayout newButton = Options.setButton(coinsButtons, curButton, getActivity(), new Options.BuyButtonClickListener() {
                @Override
                public void onClick(String id) {
                    buyItem(id);
                }
            });
            if (newButton != null) {
                purchaseButtons.add(newButton);
            }
        }


        TextView status = (TextView) root.findViewById(R.id.vip_status);
        TextView vipBtnText = (TextView) root.findViewById(R.id.fbVipBtnText);

        RelativeLayout vipBtn = (RelativeLayout) root.findViewById(R.id.fbVipButton);

        if (CacheProfile.premium) {
            status.setText(getString(R.string.vip_state_on));
            vipBtnText.setText(R.string.vip_abilities);
        } else {
            status.setText(R.string.vip_state_off);
            vipBtnText.setText(R.string.vip_advantages);
        }

        vipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToVipSettings();
            }
        });

        RelativeLayout vipTitle = (RelativeLayout) root.findViewById(R.id.fbVipTitle);
        View vipDivider = root.findViewById(R.id.vipDivider);


        if (CacheProfile.getOptions().premium.isEmpty() || CacheProfile.premium) {
            vipTitle.setVisibility(View.GONE);
            vipBtn.setVisibility(View.GONE);
            vipDivider.setVisibility(View.GONE);
        } else {
            vipTitle.setVisibility(View.VISIBLE);
            vipBtn.setVisibility(View.VISIBLE);
        }

        // Button for offerwalls (Tapjoy and Sponsorpay)
        View offerwall = root.findViewById(R.id.btnOfferwall);
        offerwall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Offerwalls.startOfferwall(getActivity());
            }
        });
        offerwall.setVisibility(CacheProfile.paid ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.titleSpecialOffers).setVisibility(CacheProfile.paid ? View.GONE : View.VISIBLE);

    }

    private void goToVipSettings() {
        Intent intent = new Intent(getActivity(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
        startActivity(intent);
    }

    @Override
    public void onInAppBillingSupported() {
        for (RelativeLayout btn : purchaseButtons) {
            btn.setEnabled(true);
        }
    }

    @Override
    public void onSubscritionSupported() {
        //TODO: добавить поддержку подписок
    }

    @Override
    public void onInAppBillingUnsupported() {
        for (RelativeLayout btn : purchaseButtons) {
            btn.setEnabled(false);
        }
        Toast.makeText(App.getContext(), getString(R.string.buy_play_market_not_available), Toast.LENGTH_SHORT).show();
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
