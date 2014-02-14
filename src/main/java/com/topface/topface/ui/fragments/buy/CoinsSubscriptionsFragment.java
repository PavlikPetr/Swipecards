package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.billing.BillingFragment;
import com.topface.topface.R;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.data.GooglePlayProducts.ProductsInfo.CoinsSubscriptionInfo;
import com.topface.topface.data.GooglePlayProducts.ProductsInfo.CoinsSubscriptionInfo.MonthInfo;
import com.topface.topface.utils.CacheProfile;

/**
 * Created by kirussell on 12.02.14.
 *
 */
public class CoinsSubscriptionsFragment extends BillingFragment {

    private LinearLayout mContainer;

    public static CoinsSubscriptionsFragment newInstance(String from) {
        CoinsSubscriptionsFragment fragment = new CoinsSubscriptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG_SOURCE, from);
        fragment.setArguments(args);
        return fragment;
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

    private void initButtonsViews(GooglePlayProducts products) {
        for (GooglePlayProducts.BuyButton curBtn : products.coinsSubscriptions) {
            GooglePlayProducts.setButton(mContainer, curBtn, getActivity(),
                    new GooglePlayProducts.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buySubscription(id);
                            Bundle arguments = getArguments();
                            String from = "";
                            if (arguments != null) {
                                from = "From" + arguments.getString(ARG_TAG_SOURCE);
                            }
                            EasyTracker.getTracker().sendEvent("Subscription", "ButtonClick" + from, id, 0L);
                        }
                    });
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
                arrowView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
                arrowView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPurchased() {

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

    }
}