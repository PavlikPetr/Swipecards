package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.billing.OpenIabFragment;
import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.Options;
import com.topface.topface.data.PaymentWallProducts;
import com.topface.topface.data.Products;
import com.topface.topface.data.PurchasesTabData;
import com.topface.topface.data.experiments.TopfaceOfferwallRedirect;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.adapters.PurchasesFragmentsAdapter;
import com.topface.topface.ui.fragments.buy.PurchasesConstants;
import com.topface.topface.ui.views.TabLayoutCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.functions.Action1;

public class PurchasesFragment extends BaseFragment {

    @Inject
    TopfaceAppState mAppState;
    public static final String IS_VIP_PRODUCTS = "is_vip_products";
    public static final String LAST_PAGE = "LAST_PAGE";
    public static final String ARG_TAG_EXRA_TEXT = "extra_text";
    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_NONE = 0;
    public static final int TYPE_GIFT = 1;
    public static final int TYPE_LEADERS = 2;
    public static final int TYPE_UNLOCK_SYMPATHIES = 3;
    public static final int TYPE_ADMIRATION = 4;
    public static final int TYPE_PEOPLE_NEARBY = 5;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";
    private static final String SKIP_BONUS = "SKIP_BONUS";
    @Bind(R.id.purchasesPager)
    ViewPager mPager;
    private PurchasesFragmentsAdapter mPagerAdapter;
    private BroadcastReceiver mVipPurchasedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setResourceInfoText(null);
        }
    };
    private TextView mCurCoins;
    private TextView mCurLikes;
    private boolean mSkipBonus;
    private boolean mIsVip;
    private String mResourceInfoText;
    private BalanceData mBalanceData;
    private Action1<BalanceData> mBalanceAction = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mBalanceData = balanceData;
            updateBalanceCounters(balanceData);
        }
    };
    private Subscription mBalanceSubscription;
    @Bind(R.id.purchasesTabs)
    TabLayout mTabLayout;
    private TabLayoutCreator mTabLayoutCreator;
    private ArrayList<String> mPagesTitle = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction);
        if (savedInstanceState != null) {
            mSkipBonus = savedInstanceState.getBoolean(SKIP_BONUS, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.purchases_fragment, null);
        ButterKnife.bind(this, root);
        initViews(root, savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mVipPurchasedReceiver, new IntentFilter(CountersManager.UPDATE_VIP_STATUS));
        mTabLayoutCreator = new TabLayoutCreator(getActivity(), mPager, mTabLayout, mPagesTitle, null);
        mTabLayoutCreator.setTabTitle(mPager.getCurrentItem());
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(LAST_PAGE, mPager.getCurrentItem());
        outState.putBoolean(SKIP_BONUS, isBonusSkiped());
    }

    @Override
    public void onResume() {
        super.onResume();
        setResourceInfoText(mBalanceData.premium ? null : getInfoText());
        updateBalanceCounters(mBalanceData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mVipPurchasedReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBalanceSubscription.unsubscribe();
    }

    public boolean forceBonusScreen(String infoText) {
        if (!isBonusSkiped()) {
            int bonusTabIndex = mPagerAdapter.getTabIndex(PurchasesTabData.BONUS);
            if (isBonusPageAvailable()) {
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

    public boolean isBonusPageAvailable() {
        return null != mPagerAdapter && mPagerAdapter.hasTab(PurchasesTabData.BONUS) && mPager.getCurrentItem() != mPagerAdapter.getTabIndex(PurchasesTabData.BONUS);
    }

    public boolean isBonusSkiped() {
        return mSkipBonus;
    }

    private void createTabList(LinkedList<PurchasesTabData> list) {
        for (PurchasesTabData tab : list)
            mPagesTitle.add(tab.name.toUpperCase());
    }

    private void initViews(View root, Bundle savedInstanceState) {
        Bundle args = getArguments();
        mIsVip = args.getBoolean(IS_VIP_PRODUCTS, false);
        args.putString(PurchasesConstants.ARG_RESOURCE_INFO_TEXT, mResourceInfoText == null ? getInfoText() : mResourceInfoText);

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
        createTabList(tabs.list);
        mPagerAdapter = new PurchasesFragmentsAdapter(getChildFragmentManager(), args, tabs.list);
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private TopfaceOfferwallRedirect mTopfaceOfferwallRedirect = CacheProfile.getOptions().topfaceOfferwallRedirect;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mTabLayoutCreator != null) {
                    mTabLayoutCreator.setTabTitle(position);
                }
                setResourceInfoText();
                if (position == mPagerAdapter.getTabIndex(PurchasesTabData.BONUS)) {
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
        initBalanceCounters(getSupportActionBar().getCustomView());
        setResourceInfoText();
        if (savedInstanceState != null) {
            mPager.setCurrentItem(savedInstanceState.getInt(LAST_PAGE, 0));
        } else {
            mPager.setCurrentItem(0);
        }
    }

    private void removeExcessTabs(LinkedList<PurchasesTabData> tabs) {
        boolean isVip = getArguments().getBoolean(IS_VIP_PRODUCTS, false);
        for (Iterator<PurchasesTabData> iterator = tabs.iterator(); iterator.hasNext(); ) {
            PurchasesTabData tab = iterator.next();
            //Удаляем вкладку Google Play, если не доступны Play Services
            if (TextUtils.equals(tab.type, PurchasesTabData.GPLAY) && !new GoogleMarketApiManager().isMarketApiAvailable()) {
                iterator.remove();
            } else {
                Products products = getProductsByTab(tab);
                if (products != null) {
                    if ((!isVip && products.coins.isEmpty() && products.likes.isEmpty()) ||
                            (isVip && products.premium.isEmpty()) || !PurchasesTabData.markets.contains(tab.type)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private Products getProductsByTab(PurchasesTabData tab) {
        Products products = null;
        switch (tab.type) {
            case PurchasesTabData.GPLAY:
                products = CacheProfile.getMarketProducts();
                break;
            case PurchasesTabData.PWALL:
                products = CacheProfile.getPaymentWallProducts(PaymentWallProducts.TYPE.DIRECT);
                break;
            case PurchasesTabData.PWALL_MOBILE:
                products = CacheProfile.getPaymentWallProducts(PaymentWallProducts.TYPE.MOBILE);
                break;
        }
        return products;
    }

    private void initBalanceCounters(View root) {
        final LinearLayout containerView = (LinearLayout) root.findViewById(R.id.resources_layout);
        containerView.setVisibility(View.VISIBLE);
        containerView.post(new Runnable() {
            @Override
            public void run() {
                int containerWidth = containerView.getMeasuredWidth();
                if (mCurCoins != null && mCurLikes != null) {
                    mCurCoins.setMaxWidth(containerWidth / 2);
                    mCurLikes.setMaxWidth(containerWidth / 2);
                }
            }
        });
        mCurCoins = (TextView) root.findViewById(R.id.coins_textview);
        mCurLikes = (TextView) root.findViewById(R.id.likes_textview);
        mCurCoins.setSelected(true);
        mCurLikes.setSelected(true);
        updateBalanceCounters(mBalanceData);
    }

    private void updateBalanceCounters(BalanceData balance) {
        if (mCurCoins != null && mCurLikes != null && balance != null) {
            mCurCoins.setText(Integer.toString(balance.money));
            mCurLikes.setText(Integer.toString(balance.likes));
        }
    }

    private String getInfoText() {
        String text;
        Bundle args = getArguments();
        if (args != null) {
            int type = args.getInt(ARG_ITEM_TYPE);
            int coins = args.getInt(ARG_ITEM_PRICE);
            int diff = coins - mBalanceData.money;
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
        return getString(R.string.purchase_header_title);
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

    private Intent getUpdateResourceInfoTextIntent(String text) {
        Intent intent = new Intent(OpenIabFragment.UPDATE_RESOURCE_INFO);
        intent.putExtra(PurchasesConstants.ARG_RESOURCE_INFO_TEXT, text);
        return intent;
    }
}
