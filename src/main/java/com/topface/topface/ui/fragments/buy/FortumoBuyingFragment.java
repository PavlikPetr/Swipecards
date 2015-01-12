package com.topface.topface.ui.fragments.buy;

import android.content.Context;
import android.os.Bundle;

import com.topface.topface.data.Products;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.FortumoStore;

/**
 * Фрагмент покупки монет и симпатий через Fortumo
 */
public class FortumoBuyingFragment extends MarketBuyingFragment {

    public static final int FORTUMO_BUYING_REQUEST = 1002;

    public static FortumoBuyingFragment newInstance(String from) {
        FortumoBuyingFragment fragment = new FortumoBuyingFragment();
        Bundle args = new Bundle();
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Products getProducts() {
        return CacheProfile.getFortumoProducts();
    }

    @Override
    protected void addAvailableStores(Context context, OpenIabHelper.Options.Builder optsBuilder) {
        //Используем всю ту же логику что в MarketBuyingFragment, но устанавливаем свой тип магазина
        optsBuilder.addAvailableStores(new FortumoStore(context));
        optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_FORTUMO);
    }

    @Override
    protected int getRequestCode() {
        return FORTUMO_BUYING_REQUEST;
    }
}
