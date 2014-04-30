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
import android.widget.TextView;
import android.widget.Toast;
import com.topface.billing.BillingFragment;
import com.topface.billing.BillingType;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.data.GooglePlayProducts.ProductsInfo.CoinsSubscriptionInfo;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.PaymentwallActivity;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.topface.topface.data.GooglePlayProducts.BuyButton;
import static com.topface.topface.data.GooglePlayProducts.BuyButtonClickListener;

public class BuyingFragment extends BillingFragment {
    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_GIFT = 1;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";

    private LinkedList<View> purchaseButtons = new LinkedList<>();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CountersManager.UPDATE_BALANCE:
                    updateBalanceCounters();
                    break;
                case GooglePlayProducts.INTENT_UPDATE_PRODUCTS:
                    updateCoinsSubscriptionButton();
                    break;
            }
        }
    };

    private ServicesTextView mCurCoins;
    private ServicesTextView mCurLikes;
    private TextView mResourcesInfo;
    private String mFrom;
    private View mCoinsSubscriptionButton;
    private BuyButtonClickListener mCoinsSubscriptionClickListener = new BuyButtonClickListener() {
        @Override
        public void onClick(String id) {
            startActivityForResult(ContainerActivity.getCoinsSubscriptionIntent(mFrom), ContainerActivity.INTENT_COINS_SUBSCRIPTION_FRAGMENT);
        }
    };

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
        OfferwallsManager.init(getActivity());
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(CountersManager.UPDATE_BALANCE);
        filter.addAction(GooglePlayProducts.INTENT_UPDATE_PRODUCTS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
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

    private void updateCoinsSubscriptionButton() {
        if (mCoinsSubscriptionButton != null) {
            CoinsSubscriptionInfo coinsSubscriptionInfo = CacheProfile.getGooglePlayProducts()
                    .info.coinsSubscription;
            BuyButton btn = coinsSubscriptionInfo.getSubscriptionButton();
            GooglePlayProducts.switchOpenButtonTexts(mCoinsSubscriptionButton, btn, mCoinsSubscriptionClickListener);
        }
    }

    private void initButtons(View root) {
        LinearLayout likesButtons = (LinearLayout) root.findViewById(R.id.fbLikes);
        GooglePlayProducts products = CacheProfile.getGooglePlayProducts();
        if (products.likes.isEmpty() && products.coins.isEmpty()) {
            root.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        }
        // sympathies title
        root.findViewById(R.id.likes_title).setVisibility(
                products.likes.isEmpty() ? View.GONE : View.VISIBLE
        );
        // sympathies buttons
        for (BuyButton curButton : products.likes) {
            View btnView = GooglePlayProducts.setBuyButton(likesButtons, curButton, getActivity(),
                    new BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buyItem(id);
                        }
                    }
            );
            if (btnView != null) {
                purchaseButtons.add(btnView);
            }
        }
        // coins buttons
        initCoinsButtons(root, products);
        // paymentwall buttons
        initPaymentwallButtons(root);
    }

    private void initCoinsButtons(View root, GooglePlayProducts products) {
        boolean coinsMaskedExperiment = CacheProfile.getOptions().forceCoinsSubscriptions;
        List<BuyButton> coinsProducts = getCoinsProducts(products, coinsMaskedExperiment);
        root.findViewById(R.id.coins_title).setVisibility(
                coinsProducts.isEmpty() ? View.GONE : View.VISIBLE
        );
        LinearLayout coinsButtonsContainer = (LinearLayout) root.findViewById(R.id.fbCoins);
        if (coinsButtonsContainer.getChildCount() > 0) {
            coinsButtonsContainer.removeAllViews();
        }
        // coins subscriptions button
        mCoinsSubscriptionButton = coinsMaskedExperiment ? null : getCoinsSubscriptionsButton(products, coinsButtonsContainer);
        if (mCoinsSubscriptionButton != null) {
            purchaseButtons.add(mCoinsSubscriptionButton);
        }
        // coins items buttons also coinsSubscriptionsMasked buttons
        for (final BuyButton curButton : coinsProducts) {
            View btnView = GooglePlayProducts.setBuyButton(coinsButtonsContainer, curButton, getActivity(),
                    new BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buy(curButton);
                        }
                    }
            );
            if (btnView != null) {
                purchaseButtons.add(btnView);
            }
        }
        coinsButtonsContainer.requestLayout();
    }

    private LinkedList<BuyButton> getCoinsProducts(@NotNull GooglePlayProducts products, boolean coinsMaskedExperiment) {
        boolean hasMaskedCoinsSubs = products.info != null
                && products.info.coinsSubscriptionMasked != null
                && products.info.coinsSubscriptionMasked.status != null
                && products.info.coinsSubscriptionMasked.status.isActive();
        return coinsMaskedExperiment && !hasMaskedCoinsSubs ? products.coinsSubscriptionsMasked : products.coins;
    }

    private View getCoinsSubscriptionsButton(GooglePlayProducts products, LinearLayout coinsButtons) {
        if (!products.coinsSubscriptions.isEmpty()) {
            CoinsSubscriptionInfo info = products.info.coinsSubscription;
            BuyButton btn = info.status.isActive() ? info.hasSubscriptionButton : info.noSubscriptionButton;
            return GooglePlayProducts.setOpenButton(coinsButtons, btn,
                    getActivity(), mCoinsSubscriptionClickListener);
        }
        return null;
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
        } else if (requestCode == ContainerActivity.INTENT_COINS_SUBSCRIPTION_FRAGMENT) {
            if (resultCode == PaymentwallActivity.RESULT_OK) {
                updateCoinsSubscriptionButton();
            }
        }
    }

    @Override
    public void onInAppBillingSupported() {
        for (View btn : purchaseButtons) {
            btn.setEnabled(true);
        }
    }

    @Override
    public void onSubscriptionSupported() {
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
    public void onSubscriptionUnsupported() {
        //TODO: добавить поддержку подписок
    }

    @Override
    public void onPurchased(String productId) {
        Debug.log("Purchased item with ID:" + productId);
        if (CacheProfile.getGooglePlayProducts().isSubscription(productId)) {
            App.sendProfileAndOptionsRequests(new SimpleApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    super.success(response);
                    if (isAdded()) {
                        initCoinsButtons(getView(), CacheProfile.getGooglePlayProducts());
                    }
                    LocalBroadcastManager.getInstance(App.getContext())
                            .sendBroadcast(new Intent(GooglePlayProducts.INTENT_UPDATE_PRODUCTS));
                }
            });
        }
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
