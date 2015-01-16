package com.topface.topface.ui.fragments.buy;

import android.content.Context;
import android.os.Bundle;

import com.topface.topface.data.Products;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.FortumoStore;

/**
 * Фрагмент покупки VIP через Fortumo
 */
public class VipFortumoBuyFragment extends VipBuyFragment {
    public static VipFortumoBuyFragment newInstance(boolean needActionBar, String from) {
        VipFortumoBuyFragment fragment = new VipFortumoBuyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ACTION_BAR_CONST, needActionBar);
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void addAvailableStores(Context context, OpenIabHelper.Options.Builder optsBuilder) {
        //Используем всю ту же логику что в MarketBuyingFragment, но устанавливаем свой тип магазина
        optsBuilder.addAvailableStores(new FortumoStore(context));
        optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_FORTUMO);
    }

    @Override
    protected Products getProducts() {
        return CacheProfile.getFortumoProducts();
    }

    @Override
    public void onSubscriptionUnsupported() {

    }

    @Override
    protected int getRequestCode() {
        return FortumoBuyingFragment.FORTUMO_BUYING_REQUEST;
    }
}
