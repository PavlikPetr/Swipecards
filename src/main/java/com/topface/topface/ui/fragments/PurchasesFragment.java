package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.billing.OpenIabFragment;
import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.ui.adapters.PurchasesFragmentsAdapter;
import com.topface.topface.ui.views.slidingtab.SlidingTabLayout;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.Utils;

import java.util.Iterator;
import java.util.LinkedList;

public class PurchasesFragment extends BaseFragment {

    public static final String IS_VIP_PRODUCTS = "is_vip_products";
    public static final String LAST_PAGE = "LAST_PAGE";
    public static final String ARG_TAG_EXRA_TEXT = "extra_text";
    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_NONE = 0;
    public static final int TYPE_GIFT = 1;
    public static final int TYPE_LEADERS = 2;
    public static final int TYPE_UNLOCK_SYMPATHIES = 3;
    public static final int TYPE_ADMIRATION = 4;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";
    private static final String SKIP_BONUS = "SKIP_BONUS";
    private ViewPager mPager;
    private PurchasesFragmentsAdapter mPagerAdapter;
    private BroadcastReceiver mVipPurchasedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setResourceInfoVisibility(View.GONE);
        }
    };
    private TextView mCurCoins;
    private TextView mCurLikes;
    private boolean mSkipBonus;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBalanceCounters();
            setResourceInfoText();
        }
    };
    private boolean mIsVip;
    private String mResourceInfoText;
    private int mResourceInfoVisibility = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSkipBonus = savedInstanceState.getBoolean(SKIP_BONUS, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.purchases_fragment, null);
        initViews(root, savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(CountersManager.UPDATE_BALANCE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mVipPurchasedReceiver, new IntentFilter(CountersManager.UPDATE_VIP_STATUS));
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(LAST_PAGE, mPager.getCurrentItem());
        outState.putBoolean(SKIP_BONUS, mSkipBonus);
    }

    @Override
    public void onResume() {
        super.onResume();
        setResourceInfoVisibility(CacheProfile.premium ? View.GONE : View.VISIBLE);
        updateBalanceCounters();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mVipPurchasedReceiver);
    }

    public boolean forceBonusScreen(String infoText) {
        if (!mSkipBonus) {
            int bonusTabIndex = mPagerAdapter.getTabIndex(Options.Tab.BONUS);
            if (mPagerAdapter.hasTab(Options.Tab.BONUS) && mPager.getCurrentItem() != bonusTabIndex) {
                mPager.setCurrentItem(bonusTabIndex);
                setResourceInfoText(infoText);
                return true;
            }
        }
        return false;
    }

    public void skipBonus() {
        mSkipBonus = true;
    }

    private void initViews(View root, Bundle savedInstanceState) {
        SlidingTabLayout tabIndicator = (SlidingTabLayout) root.findViewById(R.id.purchasesTabs);
        tabIndicator.setUseWeightProportions(true);
        tabIndicator.setCustomTabView(R.layout.tab_indicator, R.id.tab_title);
        mPager = (ViewPager) root.findViewById(R.id.purchasesPager);

        Bundle args = getArguments();
        mIsVip = args.getBoolean(IS_VIP_PRODUCTS, false);
        args.putString(OpenIabFragment.ARG_RESOURCE_INFO_TEXT, mResourceInfoText == null ? getInfoText() : mResourceInfoText);
        args.putInt(OpenIabFragment.ARG_RESOURCE_INFO_VISIBILITY, mResourceInfoVisibility == -1 ? View.VISIBLE : mResourceInfoVisibility);

        Options.TabsList tabs;
        //Для того, что бы при изменении текста плавно менялся лейаут, без скачков
        Utils.enableLayoutChangingTransition((ViewGroup) root.findViewById(R.id.purchaseLayout));
        if (mIsVip) {
            tabs = new Options.TabsList();
            tabs.list.addAll(CacheProfile.getOptions().premiumTabs.list);
        } else {
            tabs = new Options.TabsList();
            tabs.list.addAll(CacheProfile.getOptions().otherTabs.list);
        }

        removeExcessTabs(tabs.list); //Убираем табы в которых нет продуктов и бонусную вкладку, если фрагмент для покупки випа

        mPagerAdapter = new PurchasesFragmentsAdapter(getChildFragmentManager(), args, tabs.list);
        mPager.setAdapter(mPagerAdapter);
        tabIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private TopfaceOfferwallRedirect mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setResourceInfoText();
                if (position == mPagerAdapter.getTabIndex(Options.Tab.BONUS)) {
                    if (mTopfaceOfferwallRedirect != null && mTopfaceOfferwallRedirect.isEnabled()) {
                        StatisticsTracker.getInstance().sendEvent("bonuses_opened",
                                new Slices().putSlice("ref", mTopfaceOfferwallRedirect.getGroup()));
                        mTopfaceOfferwallRedirect.setComplited(true);
                    }
                    mSkipBonus = true;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabIndicator.setViewPager(mPager);
        initBalanceCounters(getSupportActionBar().getCustomView());
        setResourceInfoText();
        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(LAST_PAGE, 0));
        } else {
            mPager.setCurrentItem(0);
        }
    }

    private void removeExcessTabs(LinkedList<Options.Tab> tabs) {
        boolean isVip = getArguments().getBoolean(IS_VIP_PRODUCTS, false);
        Options.Tab pwallMobileTab = null;
        Options.Tab fortimoTab = null;
        for (Iterator<Options.Tab> iterator = tabs.iterator(); iterator.hasNext(); ) {
            Options.Tab tab = iterator.next();
            switch (tab.type) {
                case Options.Tab.PWALL_MOBILE:
                    pwallMobileTab = tab;
                    break;
            }
            //Удаляем вкладку Google Play, если не доступны Play Services
            if (TextUtils.equals(tab.type, Options.Tab.GPLAY) && !new GoogleMarketApiManager().isMarketApiAvailable()) {
                iterator.remove();
            } else {
                Products products = getProductsByTab(tab);
                if (products != null) {
                    if ((!isVip && products.coins.isEmpty() && products.likes.isEmpty()) ||
                            (isVip && products.premium.isEmpty()) || !Options.Tab.markets.contains(tab.type)) {
                        iterator.remove();
                    }
                }
            }
        }
        if (tabs.contains(fortimoTab)) {
            tabs.remove(pwallMobileTab);
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
        if (mCurCoins != null && mCurLikes != null) {
            mCurCoins.setText(Integer.toString(CacheProfile.money));
            mCurLikes.setText(Integer.toString(CacheProfile.likes));
        }
    }

    private String getInfoText() {
        String text;
        Bundle args = getArguments();
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
                case TYPE_ADMIRATION:
                    text = Utils.getQuantityString(R.plurals.buying_admiration_you_need_coins, diff, diff);
                    break;
                default:
                    if (coins != 0) {
                        text = Utils.getQuantityString(R.plurals.buying_you_need_coins, diff, diff);
                    } else {
                        if (extraText != null) {
                            text = extraText;
                        } else {
                            text = getResources().getString(mIsVip ? R.string.vip_state_off : R.string.buying_default_message);
                        }
                    }

                    break;
            }
            if (diff <= 0 && type != TYPE_NONE) {
                text = getResources().getString(R.string.buying_default_message);
            }
        } else {
            text = getResources().getString(R.string.buying_default_message);
        }
        return text;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.buying_header_title);
    }

    public boolean isVipProducts() {
        return mIsVip;
    }

    private void setResourceInfoText(String text) {
        mResourceInfoText = text;
        sendResourceInfoTextBroadcast();
    }

    private void sendResourceInfoTextBroadcast() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(getUpdateResourceInfoTextIntent(mResourceInfoText));
    }

    private void setResourceInfoText() {
        setResourceInfoText(getInfoText());
    }

    private void setResourceInfoVisibility(int visibility) {
        mResourceInfoVisibility = visibility;
        sendResourceInfoVisibilityBroadcast();
    }

    private void sendResourceInfoVisibilityBroadcast() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(getUpdateResourceInfoTextIntent(mResourceInfoVisibility));
    }

    private Intent getUpdateResourceInfoTextIntent(String text) {
        Intent intent = new Intent(OpenIabFragment.UPDATE_RESOURCE_INFO);
        intent.putExtra(OpenIabFragment.ARG_RESOURCE_INFO_TEXT, text);
        return intent;
    }

    private Intent getUpdateResourceInfoTextIntent(int visibility) {
        Intent intent = new Intent(OpenIabFragment.UPDATE_RESOURCE_INFO);
        intent.putExtra(OpenIabFragment.ARG_RESOURCE_INFO_VISIBILITY, visibility);
        return intent;
    }
}
