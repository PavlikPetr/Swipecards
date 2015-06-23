package com.topface.topface.ui.fragments.buy;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.LinearLayout;

import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.CoinsSubscriptionsActivity;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.List;

public abstract class GoogleMarketBuyingFragment extends OpenIabFragment {
    private View mCoinsSubscriptionButton;


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
