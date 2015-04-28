package com.topface.topface.ui.fragments.buy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.billing.OpenIabFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.data.Products.ProductsInfo.CoinsSubscriptionInfo;
import com.topface.topface.data.Products.ProductsInfo.CoinsSubscriptionInfo.MonthInfo;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;

import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirussell on 12.02.14.
 * Subscriptions on packs of coins.
 * UI configures based on server options from Products object
 */
public class CoinsSubscriptionsFragment extends OpenIabFragment {
    private LinearLayout mContainer;
    private List<View> mButtonsViews = new ArrayList<>();
    private TextView mResourceInfo;
    private String mResourceInfoText;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.hasExtra(ARG_RESOURCE_INFO_TEXT)) {
                    mResourceInfoText = intent.getExtras().getString(ARG_RESOURCE_INFO_TEXT);
                    setResourceInfoText();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(OpenIabFragment.UPDATE_RESOURCE_INFO));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_coins_subscription, null);
        mContainer = (LinearLayout) root.findViewById(R.id.loContainer);
        mResourceInfo = (TextView) root.findViewById(R.id.payReasonFragmentCoinsSubsription);
        setResourceInfoText();
        Products products = getProducts();
        if (products != null) {
            CoinsSubscriptionInfo info = products.info.coinsSubscription;
            // info text
            ((TextView) root.findViewById(R.id.tvInfoText)).setText(info.text);
            // icons with coins
            initCoinsIconsViews(root, info);
            // buttons
            initButtonsViews(products);
        }
        return root;
    }

    @Override
    protected Products getProducts() {
        return CacheProfile.getMarketProducts();
    }

    private void removeAllBuyButtons() {
        for (View button : mButtonsViews) {
            mContainer.removeView(button);
        }
        mButtonsViews.clear();
    }

    private void initButtonsViews(Products products) {
        for (final Products.BuyButton curBtn : products.coinsSubscriptions) {
            View buyBtn = Products.setBuyButton(mContainer, curBtn, getActivity(),
                    new Products.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            if (curBtn instanceof Products.SubscriptionBuyButton) {
                                if (((Products.SubscriptionBuyButton) curBtn).activated) {
                                    Toast.makeText(getActivity(), R.string.subscriptions_can_be_changed, Toast.LENGTH_SHORT)
                                            .show();
                                    return;
                                }
                            }
                            buy(curBtn);
                            Bundle arguments = getArguments();
                            String from = "";
                            if (arguments != null) {
                                from = "From" + arguments.getString(ARG_TAG_SOURCE);
                            }
                            EasyTracker.sendEvent("Coins Subscription", "ButtonClick" + from, id, 0L);
                        }
                    }
            );
            mButtonsViews.add(buyBtn);
            buyBtn.setTag(curBtn);
        }
    }

    private void initCoinsIconsViews(View root, CoinsSubscriptionInfo info) {
        TextView textView;
        View arrowView;
        int[] monthsResIds = new int[]{R.id.tvFirstMonth, R.id.tvSecondMonth, R.id.tvThirdMonth};
        int[] monthsArrowsResIds = new int[]{0, R.id.ivSecondMonthArrow, R.id.ivThirdMonthArrow};
        for (int i = 0; i < monthsResIds.length; i++) {
            MonthInfo month = info.months.get(i);
            //noinspection ResourceType
            textView = (TextView) root.findViewById(monthsResIds[i]);
            //noinspection ResourceType
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
    public void onPurchased(final Purchase product) {
        super.onPurchased(product);
        Activity activity = getActivity();
        if (activity != null) {
            activity.setResult(Activity.RESULT_OK);
            App.sendProfileAndOptionsRequests(new SimpleApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    super.success(response);
                    if (isAdded()) {
                        removeAllBuyButtons();
                        Products products = getProducts();
                        if (products != null) {
                            initButtonsViews(products);
                        }
                    }
                    LocalBroadcastManager.getInstance(App.getContext())
                            .sendBroadcast(new Intent(Products.INTENT_UPDATE_PRODUCTS));
                }
            });
        }
    }

    private void setResourceInfoText() {
        if (mResourceInfo != null) {
            mResourceInfo.setText(mResourceInfoText);
            mResourceInfo.setVisibility(TextUtils.isEmpty(mResourceInfoText) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onInAppBillingSupported() {

    }

    @Override
    public void onSubscriptionSupported() {

    }

    @Override
    public void onInAppBillingUnsupported() {

    }

    @Override
    public void onSubscriptionUnsupported() {
    }

    @Override
    protected String getTitle() {
        return getString(R.string.coins_subscription);
    }
}