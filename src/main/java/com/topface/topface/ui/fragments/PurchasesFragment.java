package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.topface.billing.BillingFragment;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.ui.adapters.PurchasesFragmentsAdapter;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.viewpagerindicator.TabPageIndicator;

import java.util.Iterator;
import java.util.LinkedList;

public class PurchasesFragment extends BaseFragment {

    public static final String IS_VIP_PRODUCTS = "is_vip_products";
    public static final String LAST_PAGE = "LAST_PAGE";
    private TabPageIndicator mTabIndicator;
    private ViewPager mPager;
    private TextView mResourcesInfo;
    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_GIFT = 1;
    public static final int TYPE_LEADERS = 2;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";
    private TextView mCurCoins;
    private TextView mCurLikes;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                    updateBalanceCounters();
        }
    };

    public static PurchasesFragment newInstance(int type, int coins, String from) {
        PurchasesFragment fragment = new PurchasesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_TYPE, type);
        args.putInt(ARG_ITEM_PRICE, coins);
        if (from != null) {
            args.putString(BillingFragment.ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static PurchasesFragment newInstance(String from) {
        PurchasesFragment purchasesFragment = new PurchasesFragment();
        if (from != null) {
            Bundle args = new Bundle();
            args.putString(BillingFragment.ARG_TAG_SOURCE, from);
            purchasesFragment.setArguments(args);
        }
        return purchasesFragment;
    }

    public static PurchasesFragment newInstance(String extratext, String from) {
        PurchasesFragment purchasesFragment = new PurchasesFragment();
        Bundle args = new Bundle();
        args.putString(BillingFragment.ARG_TAG_SOURCE, from);
        args.putString(VipBuyFragment.ARG_TAG_EXRA_TEXT, extratext);
        args.putBoolean(IS_VIP_PRODUCTS, true);
        purchasesFragment.setArguments(args);
        return purchasesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.purchases_fragment, null);

        initViews(root, savedInstanceState);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(LAST_PAGE, mPager.getCurrentItem());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBalanceCounters();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(CountersManager.UPDATE_BALANCE));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void initViews(View root, Bundle savedInstanceState) {
        mTabIndicator = (TabPageIndicator) root.findViewById(R.id.purchasesTabs);
        mPager = (ViewPager) root.findViewById(R.id.purchasesPager);

        LinkedList<Options.Tab> tabs;
        mResourcesInfo = (TextView) root.findViewById(R.id.payReason);
        if (getArguments().getBoolean(IS_VIP_PRODUCTS)) {
            mResourcesInfo.setVisibility(View.GONE);
            tabs = new LinkedList<>(CacheProfile.getOptions().premiumTabs);
        } else {
            tabs = new LinkedList<>(CacheProfile.getOptions().otherTabs);
            mResourcesInfo.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_animation));
        }

        removeExcessTabs(tabs); //Убираем табы в которых нет продуктов и бонусную вкладку, если фрагмент для покупки випа

        PurchasesFragmentsAdapter pagerAdapter = new PurchasesFragmentsAdapter(getChildFragmentManager(), getArguments(), tabs);
        mPager.setAdapter(pagerAdapter);
        mTabIndicator.setViewPager(mPager);
        updateBalanceCounters();
        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(LAST_PAGE, 0));
        } else {
            mPager.setCurrentItem(0);
        }
        initBalanceCounters(getSupportActionBar().getCustomView());
    }

    private void removeExcessTabs(LinkedList<Options.Tab> tabs) {
        boolean isVip = getArguments().getBoolean(IS_VIP_PRODUCTS);
        for (Iterator<Options.Tab> iterator = tabs.iterator(); iterator.hasNext();) {
            Options.Tab tab = iterator.next();
            Products products = getProductsByTab(tab);
            if (products != null) {
                if ((!isVip && products.coins.isEmpty() && products.likes.isEmpty()) || (isVip && products.premium.isEmpty())) {
                    iterator.remove();
                }
            }
        }
    }

    private Products getProductsByTab(Options.Tab tab) {
        Products products = null;
        switch (tab.type) {
            case Options.Tab.GPLAY:
                products = CacheProfile.getMarketProducts();
                break;
            case Options.Tab.PWALL:
                products = CacheProfile.getPaymentWallProducts(PaymentWallProducts.TYPE.DIRECT);
                break;
            case Options.Tab.PWALL_MOBILE:
                products = CacheProfile.getPaymentWallProducts(PaymentWallProducts.TYPE.MOBILE);
                break;
        }
        return products;
    }

    private void initBalanceCounters(View root) {
        root.findViewById(R.id.resources_layout).setVisibility(View.VISIBLE);
        mCurCoins = (TextView) root.findViewById(R.id.coins_textview);
        mCurLikes = (TextView) root.findViewById(R.id.likes_textview);
        updateBalanceCounters();
    }

    private void updateBalanceCounters() {

        Bundle args = getArguments();
        if (mCurCoins != null && mCurLikes != null) {
            mCurCoins.setText(Integer.toString(CacheProfile.money));
            mCurLikes.setText(Integer.toString(CacheProfile.likes));
        }
        if (args != null) {
            int type = args.getInt(ARG_ITEM_TYPE);
            int coins = args.getInt(ARG_ITEM_PRICE);
            switch (type) {
                case TYPE_GIFT:
                    mResourcesInfo.setText(String.format(
                            getResources().getString(R.string.buying_you_have_no_coins_for_gift),
                            coins - CacheProfile.money));
                    break;
                default:
                    mResourcesInfo.setText(getResources().getString(R.string.buying_default_message));
                    break;
            }
        } else {
            mResourcesInfo.setText(getResources().getString(R.string.buying_default_message));
        }

    }

    @Override
    protected String getTitle() {
        return getString(R.string.buying_header_title);
    }

}
