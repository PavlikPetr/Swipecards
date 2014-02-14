package com.topface.topface.ui.fragments.buy;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.billing.BillingFragment;
import com.topface.billing.BillingType;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.data.GooglePlayProducts.ProductsInfo.CoinsSubscriptionInfo;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.offerwalls.Offerwalls;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class BuyingFragment extends BillingFragment {

    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_GIFT = 1;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";

    private LinkedList<RelativeLayout> purchaseButtons = new LinkedList<>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBalanceCounters();
        }
    };

    private ServicesTextView mCurCoins;
    private ServicesTextView mCurLikes;
    private TextView mResourcesInfo;
    private String mFrom;

    public static BuyingFragment newInstance(int type, int coins, String from) {
        BuyingFragment fragment = new BuyingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_TYPE, type);
        args.putInt(ARG_ITEM_PRICE, coins);
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static BuyingFragment newInstance(String from) {
        BuyingFragment buyingFragment = new BuyingFragment();
        if (from != null) {
            Bundle args = new Bundle();
            args.putString(ARG_TAG_SOURCE, from);
            buyingFragment.setArguments(args);
        }
        return buyingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Offerwalls.init(getActivity());
        Bundle args = getArguments();
        if (args != null) {
            mFrom = args.getString(ARG_TAG_SOURCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(CountersManager.UPDATE_BALANCE));
        updateBalanceCounters();
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
        GooglePlayProducts products = CacheProfile.getGooglePlayProducts();
        if (products.likes.isEmpty() && products.coins.isEmpty()) {
            root.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        }
        // likes buttons
        if (products.likes.isEmpty()) {
            root.findViewById(R.id.likes_title).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.likes_title).setVisibility(View.VISIBLE);
        }
        for (GooglePlayProducts.BuyButton curButton : products.likes) {
            RelativeLayout newButton = GooglePlayProducts.setButton(likesButtons, curButton, getActivity(),
                    new GooglePlayProducts.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buyItem(id);
                        }
                    });
            if (newButton != null) {
                purchaseButtons.add(newButton);
            }
        }
        // coins buttons
        if (products.coins.isEmpty()) {
            root.findViewById(R.id.coins_title).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.coins_title).setVisibility(View.VISIBLE);
        }
        LinearLayout coinsButtons = (LinearLayout) root.findViewById(R.id.fbCoins);
        if (!products.coinsSubscriptions.isEmpty()) {
            CoinsSubscriptionInfo info = products.productsInfo.coinsSubscriptionInfo;
            GooglePlayProducts.BuyButton btn = info.status.active ? info.hasSubscriptionButton : info.noSubscriptionButton;
            if (info.status.active) {
                RelativeLayout newButton = GooglePlayProducts.setButton(coinsButtons, btn, getActivity(),
                        new GooglePlayProducts.BuyButtonClickListener() {
                            @Override
                            public void onClick(String id) {
                                startActivity(ContainerActivity.getCoinsSubscriptionIntent(mFrom));
                            }
                        });
                if (newButton != null) {
                    purchaseButtons.add(newButton);
                }
            }
        }
        for (GooglePlayProducts.BuyButton curButton : products.coins) {
            RelativeLayout newButton = GooglePlayProducts.setButton(coinsButtons, curButton, getActivity(),
                    new GooglePlayProducts.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buyItem(id);
                        }
                    });
            if (newButton != null) {
                purchaseButtons.add(newButton);
            }
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
        // paymentwall buttons
        initPaymentwallButtons(root);
    }

    private void initPaymentwallButtons(View root) {
        //Показываем кнопку только на платформе Google Play v2
        if (BuildConfig.BILLING_TYPE == BillingType.GOOGLE_PLAY) {
            //Paymentwall
            View mobilePayments = root.findViewById(R.id.mobilePayments);
            mobilePayments.setVisibility(View.VISIBLE);
            ViewGroup layout = (ViewGroup) mobilePayments.findViewById(R.id.mobilePaymentsList);
            //Листенер просто открывае
            View.OnClickListener mobilePaymentsListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.startActivityForResult(
                                PaymentwallActivity.getIntent(activity, CacheProfile.uid),
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
        //Если платежи не поддерживаются, то скрываем все кнопки
        getView().findViewById(R.id.likes_title).setVisibility(View.GONE);
        getView().findViewById(R.id.coins_title).setVisibility(View.GONE);
        getView().findViewById(R.id.fbCoins).setVisibility(View.GONE);
        getView().findViewById(R.id.fbLikes).setVisibility(View.GONE);

        Toast.makeText(App.getContext(), R.string.buy_play_market_not_available, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscritionUnsupported() {
        //TODO: добавить поддержку подписок
    }

    @Override
    public void onPurchased() {
    }

    @Override
    public void onError() {
    }

    @Override
    public void onCancel() {
        //Возможно стоит добавить реакцию на отмену покупки пользователем
    }

    @Override
    protected String getTitle() {
        return getString(R.string.buying_header_title);
    }
}
