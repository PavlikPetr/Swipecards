package com.topface.topface.ui.fragments.buy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.CoinsSubscriptionsActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.CacheProfile;

import java.util.LinkedList;
import java.util.List;

public abstract class CoinsBuyingFragment extends OpenIabFragment {
    private LinkedList<View> purchaseButtons = new LinkedList<>();
    private View mCoinsSubscriptionButton;

    private String mFrom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(false);
        Bundle args = getArguments();
        if (args != null) {
            mFrom = args.getString(ARG_TAG_SOURCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_buy, null);
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
        // coins subscriptions button
        mCoinsSubscriptionButton = coinsMaskedExperiment ? null : getCoinsSubscriptionsButton(products, coinsButtonsContainer);
        if (mCoinsSubscriptionButton != null) {
            purchaseButtons.add(mCoinsSubscriptionButton);
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

    private void updateCoinsSubscriptionButton() {
        if (mCoinsSubscriptionButton != null) {
            Products products = getProducts();
            if (products != null) {
                Products.ProductsInfo.CoinsSubscriptionInfo coinsSubscriptionInfo = products
                        .info.coinsSubscription;
                Products.BuyButton btn = coinsSubscriptionInfo.getSubscriptionButton();
                Products.switchOpenButtonTexts(mCoinsSubscriptionButton, btn, getCoinsSubscriptionClickListener());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CoinsSubscriptionsActivity.INTENT_COINS_SUBSCRIPTION) {
            if (resultCode == Activity.RESULT_OK) {
                updateCoinsSubscriptionButton();
            }
        }
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
    public void onPurchased(String productId) {
        super.onPurchased(productId);
        Debug.log("Purchased item with ID:" + productId);
        final Products products = getProducts();
        if (products != null && products.isSubscription(productId)) {
            App.sendProfileAndOptionsRequests(new SimpleApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    super.success(response);
                    if (isAdded()) {
                        initCoinsButtons(getView(), products);
                    }
                    LocalBroadcastManager.getInstance(App.getContext())
                            .sendBroadcast(new Intent(Products.INTENT_UPDATE_PRODUCTS));
                }
            });
        }
    }

    protected abstract View getCoinsSubscriptionsButton(Products products, LinearLayout coinsButtonsContainer);

    protected abstract List<Products.BuyButton> getCoinsProducts(Products products, boolean coinsMaskedExperiment);

    public abstract Products.BuyButtonClickListener getCoinsSubscriptionClickListener();
}
