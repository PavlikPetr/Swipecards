package com.topface.topface.ui.fragments.buy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.data.GooglePlayProducts.ProductsInfo.CoinsSubscriptionInfo;
import com.topface.topface.data.GooglePlayProducts.ProductsInfo.CoinsSubscriptionInfo.MonthInfo;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.utils.CacheProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirussell on 12.02.14.
 * Subscriptions on packs of coins.
 * UI configures based on server options from GooglePlayProducts object
 */
public class CoinsSubscriptionsFragment extends BillingFragment {
    private LinearLayout mContainer;
    private List<View> mButtonsViews = new ArrayList<>();

    public static CoinsSubscriptionsFragment newInstance(String from) {
        CoinsSubscriptionsFragment fragment = new CoinsSubscriptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG_SOURCE, from);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_coins_subscription, null);
        mContainer = (LinearLayout) root.findViewById(R.id.loContainer);
        GooglePlayProducts products = CacheProfile.getGooglePlayProducts();
        CoinsSubscriptionInfo info = products.productsInfo.coinsSubscriptionInfo;
        // info text
        ((TextView) root.findViewById(R.id.tvInfoText)).setText(info.text);
        // icons with coins
        initCoinsIconsViews(root, info);
        // buttons
        initButtonsViews(products);
        return root;
    }

    private void removeAllBuyButtons() {
        for (View button : mButtonsViews) {
            mContainer.removeView(button);
        }
        mButtonsViews.clear();
    }

    private void initButtonsViews(GooglePlayProducts products) {
        for (final GooglePlayProducts.BuyButton curBtn : products.coinsSubscriptions) {
            mButtonsViews.add(GooglePlayProducts.setBuyButton(mContainer, curBtn, getActivity(),
                    new GooglePlayProducts.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            if (curBtn instanceof GooglePlayProducts.SubscriptionBuyButton) {
                                if (((GooglePlayProducts.SubscriptionBuyButton)curBtn).activated) {
                                    Toast.makeText(getActivity(), R.string.subscriptions_can_be_changed, Toast.LENGTH_SHORT)
                                            .show();
                                    return;
                                }
                            }
                            buySubscription(id);
                            Bundle arguments = getArguments();
                            String from = "";
                            if (arguments != null) {
                                from = "From" + arguments.getString(ARG_TAG_SOURCE);
                            }
                            EasyTracker.getTracker().sendEvent("Coins Subscription", "ButtonClick" + from, id, 0L);
                        }
                    }));
        }
    }

    private void initCoinsIconsViews(View root, CoinsSubscriptionInfo info) {
        TextView textView;
        View arrowView;
        int[] monthsResIds = new int[]{R.id.tvFirstMonth, R.id.tvSecondMonth, R.id.tvThirdMonth};
        int[] monthsArrowsResIds = new int[]{0, R.id.ivSecondMonthArrow, R.id.ivThirdMonthArrow};
        for (int i = 0; i < monthsResIds.length; i++) {
            MonthInfo month = info.months.get(i);
            textView = (TextView) root.findViewById(monthsResIds[i]);
            arrowView = root.findViewById(monthsArrowsResIds[i]);
            if (month != null && i < info.months.size()) {
                textView.setText(month.title + "\n" + month.amount);
                textView.setVisibility(View.VISIBLE);
                if (arrowView != null) {
                    arrowView.setVisibility(View.VISIBLE);
                }
            } else {
                textView.setVisibility(View.GONE);
                if (arrowView != null) {
                    arrowView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onPurchased() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setResult(Activity.RESULT_OK);
        }
        App.sendProfileAndOptionsRequests(new SimpleApiHandler() {
            @Override
            public void success(IApiResponse response) {
                super.success(response);
                if (isAdded()) {
                    removeAllBuyButtons();
                    initButtonsViews(CacheProfile.getGooglePlayProducts());
                }
                LocalBroadcastManager.getInstance(App.getContext())
                        .sendBroadcast(new Intent(GooglePlayProducts.INTENT_UPDATE_PRODUCTS));
            }
        });
    }

    @Override
    public void onError() {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onInAppBillingSupported() {

    }

    @Override
    public void onSubscritionSupported() {

    }

    @Override
    public void onInAppBillingUnsupported() {

    }

    @Override
    public void onSubscritionUnsupported() {
        //Если подписка не поддерживается, сообщаем об этом пользователю
        if (!CacheProfile.premium) {
            Toast.makeText(App.getContext(), R.string.buy_play_market_not_available, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.coins_subscription);
    }
}