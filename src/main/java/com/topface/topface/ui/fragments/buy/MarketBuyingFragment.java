package com.topface.topface.ui.fragments.buy;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.statistics.PushButtonVipStatistics;
import com.topface.topface.statistics.PushButtonVipUniqueStatistics;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.CacheProfile;

import java.util.LinkedList;

@FlurryOpenEvent(name = MarketBuyingFragment.PAGE_NAME)
public class MarketBuyingFragment extends CoinsBuyingFragment {

    public static final String PAGE_NAME = "buy.coins.gp";

    public static MarketBuyingFragment newInstance(String from, String text) {
        MarketBuyingFragment buyingFragment = new MarketBuyingFragment();
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(text)) {
            args.putString(PurchasesConstants.ARG_RESOURCE_INFO_TEXT, text);
        }
        if (from != null) {
            args.putString(PurchasesConstants.ARG_TAG_SOURCE, from);
        }
        buyingFragment.setArguments(args);
        return buyingFragment;
    }

    public static MarketBuyingFragment newInstance(int type, int coins, String from) {
        MarketBuyingFragment fragment = new MarketBuyingFragment();
        setArguments(type, coins, from, fragment);
        return fragment;
    }

    protected static void setArguments(int type, int coins, String from, MarketBuyingFragment fragment) {
        Bundle args = new Bundle();
        args.putInt(PurchasesFragment.ARG_ITEM_TYPE, type);
        args.putInt(PurchasesFragment.ARG_ITEM_PRICE, coins);
        if (from != null) {
            args.putString(PurchasesConstants.ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
    }

    protected LinkedList<BuyButtonData> getCoinsProducts(@NonNull Products products, boolean coinsMaskedExperiment) {
        boolean hasMaskedCoinsSubs = products.info != null
                && products.info.coinsSubscriptionMasked != null
                && products.info.coinsSubscriptionMasked.status != null
                && products.info.coinsSubscriptionMasked.status.isActive();
        return coinsMaskedExperiment &&
                !hasMaskedCoinsSubs &&
                products.coinsSubscriptionsMasked.size() > 0 ?
                products.coinsSubscriptionsMasked :
                products.coins;
    }

    @Override
    public Products getProducts() {
        return CacheProfile.getMarketProducts();
    }


    @Override
    public void onInAppBillingUnsupported() {
        //Если платежи не поддерживаются, то скрываем все кнопки
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.fbCoins).setVisibility(View.GONE);
            view.findViewById(R.id.fbLikes).setVisibility(View.GONE);
            view.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void buy(BuyButtonData btn) {
        PushButtonVipUniqueStatistics.sendPushButtonNoVip(btn.id, ((Object) this).getClass().getSimpleName(), getFrom(), App.from(getActivity()).getProfile());
        PushButtonVipStatistics.send(btn.id, ((Object) this).getClass().getSimpleName(), getFrom());
        super.buy(btn);
    }
}
