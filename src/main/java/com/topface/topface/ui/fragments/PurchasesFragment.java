package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.ui.adapters.PurchasesFragmentsAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;
import com.viewpagerindicator.TabPageIndicator;

import java.util.Iterator;
import java.util.LinkedList;

public class PurchasesFragment extends BaseFragment {

    public static final String IS_VIP_PRODUCTS = "is_vip_products";
    public static final String LAST_PAGE = "LAST_PAGE";
    public static final String ARG_TAG_EXRA_TEXT = "extra_text";
    private TabPageIndicator mTabIndicator;
    private ViewPager mPager;
    private TextView mResourcesInfo;
    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_NONE = 0;
    public static final int TYPE_GIFT = 1;
    public static final int TYPE_LEADERS = 2;
    public static final int TYPE_UNLOCK_SYMPATHIES = 3;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";
    private TextView mCurCoins;
    private TextView mCurLikes;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBalanceCounters();
        }
    };
    private boolean mIsVip;

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

        Intent intent = getActivity().getIntent();

        mIsVip = intent.getBooleanExtra(IS_VIP_PRODUCTS, false);

        LinkedList<Options.Tab> tabs;
        mResourcesInfo = (TextView) root.findViewById(R.id.payReason);
        if (mIsVip) {
            tabs = new LinkedList<>(CacheProfile.getOptions().premiumTabs);
        } else {
            tabs = new LinkedList<>(CacheProfile.getOptions().otherTabs);
        }
        mResourcesInfo.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_animation));

        removeExcessTabs(tabs); //Убираем табы в которых нет продуктов и бонусную вкладку, если фрагмент для покупки випа

        PurchasesFragmentsAdapter pagerAdapter = new PurchasesFragmentsAdapter(getChildFragmentManager(), intent.getExtras(), tabs);
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
        boolean isVip = getActivity().getIntent().getBooleanExtra(IS_VIP_PRODUCTS, false);
        for (Iterator<Options.Tab> iterator = tabs.iterator(); iterator.hasNext(); ) {
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
        String text;
        Bundle args = getActivity().getIntent().getExtras();
        if (mCurCoins != null && mCurLikes != null) {
            mCurCoins.setText(Integer.toString(CacheProfile.money));
            mCurLikes.setText(Integer.toString(CacheProfile.likes));
        }
        if (args != null) {
            int type = args.getInt(ARG_ITEM_TYPE);
            int coins = args.getInt(ARG_ITEM_PRICE);
            int diff = coins - CacheProfile.money;
            String extraText = args.getString(ARG_TAG_EXRA_TEXT);
            switch (type) {
                case TYPE_GIFT:
                    text = Utils.getQuantityString(R.plurals.buying_gift_you_need_coins, diff, diff);
                    break;
                case TYPE_LEADERS:
                    text = Utils.getQuantityString(R.plurals.buying_leaders_you_need_coins, diff, diff);
                    break;
                case TYPE_UNLOCK_SYMPATHIES:
                    text = Utils.getQuantityString(R.plurals.buying_unlock_likes_you_need_coins, diff, diff);
                    break;
                default:
                    if (extraText != null) {
                        text = extraText;
                    } else {
                        text = getResources().getString(mIsVip ? R.string.vip_state_off : R.string.buying_default_message);
                    }
                    break;
            }
            if (diff <= 0 && type != TYPE_NONE) {
                text = getResources().getString(R.string.buying_default_message);
            }
        } else {
            text = getResources().getString(R.string.buying_default_message);
        }

        mResourcesInfo.setText(text);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.buying_header_title);
    }

}
