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

import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.LinkedList;
import java.util.List;

public abstract class CoinsBuyingFragment extends OpenIabFragment {
    private LinkedList<View> purchaseButtons = new LinkedList<>();
    private TextView mResourceInfo;
    private String mResourceInfoText;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                getDataFromIntent(intent.getExtras());
            }
        }
    };

    private void getDataFromIntent(Bundle args) {
        if (args != null) {
            mFrom = args.getString(ARG_TAG_SOURCE);
            if (args.containsKey(ARG_RESOURCE_INFO_TEXT)) {
                mResourceInfoText = args.getString(ARG_RESOURCE_INFO_TEXT);
                setResourceInfoText();
            }
        }
    }

    private String mFrom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(false);
        getDataFromIntent(getArguments());
    }

    private void setResourceInfoText() {
        if (mResourceInfo != null) {
            mResourceInfo.setText(mResourceInfoText);
            mResourceInfo.setVisibility(TextUtils.isEmpty(mResourceInfoText) ? View.GONE : View.VISIBLE);
        }
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
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_buy, null);
        mResourceInfo = (TextView) root.findViewById(R.id.payReasonFragmentBuy);
        setResourceInfoText();
        initButtons(root);
        return root;
    }

    private void initButtons(View root) {
        LinearLayout likesButtons = (LinearLayout) root.findViewById(R.id.fbLikes);

        Products products = getProducts();
        //Если у нас нет продуктов, то не показываем кнопки
        if (products == null) {
            return;
        }
        if (products.likes.isEmpty() && products.coins.isEmpty()) {
            root.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        }
        // sympathies title
        root.findViewById(R.id.likes_title).setVisibility(
                products.likes.isEmpty() ? View.GONE : View.VISIBLE
        );
        // sympathies buttons
        for (final Products.BuyButton curButton : products.likes) {
            View btnView = Products.setBuyButton(likesButtons, curButton, getActivity(),
                    new Products.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buy(curButton);
                            Activity activity = getActivity();
                            if (activity instanceof PurchasesActivity) {
                                ((PurchasesActivity) activity).skipBonus();
                            }

                            CacheProfile.getOptions().topfaceOfferwallRedirect.setComplited(true);
                        }
                    }
            );
            if (btnView != null) {
                purchaseButtons.add(btnView);
                btnView.setTag(curButton);
            }
        }
        // coins buttons
        initCoinsButtons(root, products);
    }

    private void initCoinsButtons(View root, Products products) {
        if (products == null) {
            return;
        }
        boolean coinsMaskedExperiment = CacheProfile.getOptions().forceCoinsSubscriptions;
        List<Products.BuyButton> coinsProducts = getCoinsProducts(products, coinsMaskedExperiment);
        root.findViewById(R.id.coins_title).setVisibility(
                coinsProducts.isEmpty() ? View.GONE : View.VISIBLE
        );
        LinearLayout coinsButtonsContainer = (LinearLayout) root.findViewById(R.id.fbCoins);
        if (coinsButtonsContainer.getChildCount() > 0) {
            coinsButtonsContainer.removeAllViews();
        }
        // coins items buttons also coinsSubscriptionsMasked buttons
        for (final Products.BuyButton curButton : coinsProducts) {
            View btnView = Products.setBuyButton(coinsButtonsContainer, curButton, getActivity(),
                    new Products.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buy(curButton);
                            Activity activity = getActivity();
                            if (activity instanceof PurchasesActivity) {
                                ((PurchasesActivity) activity).skipBonus();
                            }

                            CacheProfile.getOptions().topfaceOfferwallRedirect.setComplited(true);
                        }
                    }
            );
            if (btnView != null) {
                purchaseButtons.add(btnView);
                btnView.setTag(curButton);
            }
        }
        coinsButtonsContainer.requestLayout();
    }

    public String getFrom() {
        return mFrom;
    }

    @Override
    public void onInAppBillingSupported() {
        for (View btn : purchaseButtons) {
            btn.setEnabled(true);
        }
    }

    @Override
    public void onSubscriptionSupported() {
        //В этом типе фрагментов подписок нет
    }


    @Override
    public void onSubscriptionUnsupported() {
        //В этом типе фрагментов подписок нет
    }

    @Override
    public void onPurchased(Purchase product) {
        super.onPurchased(product);
        Debug.log("Purchased item with ID:" + product.getSku());
        final Products products = getProducts();
        if (products != null && products.isSubscription(product)) {
            App.sendProfileAndOptionsRequests(new SimpleApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    super.success(response);
                    if (isAdded()) {
                        initCoinsButtons(getView(), getProducts());
                    }
                }
            });
        }
    }

    protected abstract List<Products.BuyButton> getCoinsProducts(Products products, boolean coinsMaskedExperiment);
}
