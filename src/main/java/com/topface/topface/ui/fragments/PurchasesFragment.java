package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.topface.billing.BillingFragment;
import com.topface.topface.R;
import com.topface.topface.ui.adapters.PurchasesFragmentsAdapter;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.viewpagerindicator.TabPageIndicator;

public class PurchasesFragment extends BaseFragment {

    private ActionBar mActionBar;
    private TabPageIndicator mTabIndicator;
    private ViewPager mPager;
    private TextView mResourcesInfo;
    public static final String ARG_ITEM_TYPE = "type_of_buying_item";
    public static final int TYPE_GIFT = 1;
    public static final int TYPE_DELIGHT = 2;
    public static final String ARG_ITEM_PRICE = "quantity_of_coins";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.purchases_fragment, null);
        mActionBar = getActionBar(root);
        mActionBar.showBackButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mActionBar.showCashInfo();
        mActionBar.setTitleText(getString(R.string.buying_header_title));
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        mTabIndicator = (TabPageIndicator) root.findViewById(R.id.purchasesTabs);

        mPager = (ViewPager) root.findViewById(R.id.purchasesPager);
        PurchasesFragmentsAdapter pagerAdapter = new PurchasesFragmentsAdapter(getChildFragmentManager(), getBuyingFragments());
        mPager.setAdapter(pagerAdapter);
        mTabIndicator.setViewPager(mPager);
        mResourcesInfo = (TextView) root.findViewById(R.id.payReason);
        mResourcesInfo.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_animation));
        updateBalanceCounters();
        mPager.setCurrentItem(0);
    }

    private void updateBalanceCounters() {

        Bundle args = getArguments();
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

    private SparseArrayCompat<BuyingPageEntity> getBuyingFragments() {
        SparseArrayCompat<BuyingPageEntity> fragments = new SparseArrayCompat<BuyingPageEntity>();
        Bundle gpArgs = new Bundle();
        if (getArguments() != null) {
            gpArgs.putString(BillingFragment.ARG_TAG_SOURCE, getArguments().getString(BillingFragment.ARG_TAG_SOURCE));
        }
        fragments.put(0, new BuyingPageEntity("Google play", gpArgs));
        fragments.put(1, new BuyingPageEntity("Бесплатно", null));
        fragments.put(2, new BuyingPageEntity("Другие способы", null));
        return fragments;
    }

    public class BuyingPageEntity {
        private String pageTitle;
        private Bundle arguments;

        public BuyingPageEntity(String pageTitle, Bundle arguments) {
            this.pageTitle = pageTitle;
            this.arguments = arguments;
        }

        public String getPageTitle() {
            return pageTitle;
        }

        public Bundle getArguments() {
            return arguments;
        }
    }
}
