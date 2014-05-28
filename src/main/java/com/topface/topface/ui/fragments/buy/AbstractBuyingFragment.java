package com.topface.topface.ui.fragments.buy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.topface.billing.BillingFragment;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractBuyingFragment extends BillingFragment{
    private LinkedList<View> purchaseButtons = new LinkedList<>();
    private View mCoinsSubscriptionButton;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case Products.INTENT_UPDATE_PRODUCTS:
                    updateCoinsSubscriptionButton();
                    break;
            }
        }
    };

    private String mFrom;

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

    protected void initViews(View root) {
        initButtons(root);
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
                        }
                    }
            );
            if (btnView != null) {
                purchaseButtons.add(btnView);
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
                        }
                    }
            );
            if (btnView != null) {
                purchaseButtons.add(btnView);
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

        if (requestCode == ContainerActivity.INTENT_COINS_SUBSCRIPTION_FRAGMENT) {
            if (resultCode == Activity.RESULT_OK) {
                updateCoinsSubscriptionButton();
            }
        }
    }


    @Override
    public void onInAppBillingUnsupported() {
        //Если платежи не поддерживаются, то скрываем все кнопки
        getView().findViewById(R.id.likes_title).setVisibility(View.GONE);
        getView().findViewById(R.id.coins_title).setVisibility(View.GONE);
        getView().findViewById(R.id.fbCoins).setVisibility(View.GONE);
        getView().findViewById(R.id.fbLikes).setVisibility(View.GONE);
        getView().findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
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
        //TODO: добавить поддержку подписок
    }



    @Override
    public void onSubscriptionUnsupported() {
        //TODO: добавить поддержку подписок
    }

    @Override
    public void onPurchased(String productId) {
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

    @Override
    public void onError() {
    }

    @Override
    public void onCancel() {
        //Возможно стоит добавить реакцию на отмену покупки пользователем
    }

    protected abstract View getCoinsSubscriptionsButton(Products products, LinearLayout coinsButtonsContainer);

    protected abstract List<Products.BuyButton> getCoinsProducts(Products products, boolean coinsMaskedExperiment);

    public abstract Products getProducts();

    public abstract Products.BuyButtonClickListener getCoinsSubscriptionClickListener();
}
