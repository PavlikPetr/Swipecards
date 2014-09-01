package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.topface.topface.data.Products;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.FortumoStore;

/**
 * Фрагмент покупки монет и симпатий через Fortumo
 */
public class FortumoBuyingFragment extends MarketBuyingFragment {
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
    protected void addAvailableStores(FragmentActivity activity, OpenIabHelper.Options.Builder optsBuilder) {
        //Используем всю ту же логику что в MarketBuyingFragment, но устанавливаем свой тип магазина
        optsBuilder.addAvailableStores(new FortumoStore(activity));
    }

}
