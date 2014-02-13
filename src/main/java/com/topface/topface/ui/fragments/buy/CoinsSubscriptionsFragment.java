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
import com.topface.topface.utils.CacheProfile;

/**
 * Created by kirussell on 12.02.14.
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
        // icons with coins
        TextView firstMonth = (TextView) root.findViewById(R.id.tvFirstMonth);
        firstMonth.setText(getString(R.string.first_month) + "\n+" + 10);
        TextView secondMonth = (TextView) root.findViewById(R.id.tvSecondMonth);
        secondMonth.setText(getString(R.string.second_month) + "\n+" + 40);
        TextView thirdMonth = (TextView) root.findViewById(R.id.tvThirdMonth);
        thirdMonth.setText(getString(R.string.third_month) + "\n+" + 80);
        // buttons
        GooglePlayProducts googlePlayProducts = CacheProfile.getGooglePlayProducts();
        for (GooglePlayProducts.BuyButton curBtn : googlePlayProducts.coinsSubscriptions) {
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
        return root;
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