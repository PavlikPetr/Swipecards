package com.topface.topface.ui.fragments.buy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.billing.IFreePurchases;
import com.topface.billing.OpenIabFragment;
import com.topface.topface.R;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.utils.CacheProfile;

import java.util.LinkedList;

public class IFreeBuyFragment extends IFreePurchases {
    public static final String ACTION_BAR_CONST = "needActionBar";
    public static final String ARG_TAG_SOURCE = "from_value";
    public static final String ARG_RESOURCE_INFO_TEXT = "resource_info_text";
    private TextView mResourceInfo;
    private String mResourceInfoText;
    private ProgressBar mProgress;
    private View mRoot;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                getDataFromIntent(intent.getExtras());
            }
        }
    };

    public static IFreeBuyFragment newInstance(boolean needActionBar, String from, String text) {
        IFreeBuyFragment fragment = new IFreeBuyFragment();
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(text)) {
            args.putString(ARG_RESOURCE_INFO_TEXT, text);
        }
        args.putBoolean(ACTION_BAR_CONST, needActionBar);
        if (from != null) {
            args.putString(ARG_TAG_SOURCE, from);
        }
        fragment.setArguments(args);
        return fragment;
    }

    private void getDataFromIntent(Bundle args) {
        if (args != null) {
            mFrom = args.getString(ARG_TAG_SOURCE);
            if (args.containsKey(ARG_RESOURCE_INFO_TEXT)) {
                mResourceInfoText = args.getString(ARG_RESOURCE_INFO_TEXT);
                setResourceInfoText();
            }
        }
    }

    private String mFrom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(false);
        getDataFromIntent(getArguments());
    }

    @Override
    public void onLibraryInitialised() {
        setProgressVisibility(false);
        if (!isLibraryInitialised()) {
            Products products = CacheProfile.getMarketProducts();
            initButtons(mRoot, products != null ? validateProducts(products.likes) : null, products != null ? validateProducts(products.coins) : null);

        }
        super.onLibraryInitialised();
    }

    private void setResourceInfoText() {
        if (mResourceInfo != null) {
            mResourceInfo.setText(mResourceInfoText);
            mResourceInfo.setVisibility(TextUtils.isEmpty(mResourceInfoText) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(OpenIabFragment.UPDATE_RESOURCE_INFO));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_buy, null);
        mRoot = root;
        root.findViewById(R.id.EditorTestStub).setVisibility(View.GONE);
        mResourceInfo = (TextView) root.findViewById(R.id.payReasonFragmentBuy);
        mProgress = (ProgressBar) root.findViewById(R.id.purchases_progress_bar);
        setProgressVisibility(true);
        setResourceInfoText();
        return root;
    }

    private void initButtons(View root, LinkedList<BuyButtonData> likes, LinkedList<BuyButtonData> coins) {
        if ((likes == null || likes.isEmpty()) && (coins == null || coins.isEmpty())) {
            showDisablePurchases();
        }
        // likes buttons
        initLikesButtons(root, likes);
        // coins buttons
        initCoinsButtons(root, coins);
    }

    private void showDisablePurchases() {
        mRoot.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        setCoinsVisibility(false);
        setLikesVisibility(false);
    }

    private void setCoinsVisibility(boolean isVisible) {
        mRoot.findViewById(R.id.coins_title).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mRoot.findViewById(R.id.fbCoins).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void setLikesVisibility(boolean isVisible) {
        mRoot.findViewById(R.id.likes_title).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mRoot.findViewById(R.id.fbLikes).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void setProgressVisibility(boolean isVisible) {
        if (mProgress != null) {
            mProgress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
        if (isVisible) {
            setCoinsVisibility(false);
            setLikesVisibility(false);
        }
    }

    private void initLikesButtons(View root, LinkedList<BuyButtonData> likes) {
        if (likes == null || likes.isEmpty()) {
            setLikesVisibility(false);
            return;
        }
        setLikesVisibility(true);
        LinearLayout likesButtons = (LinearLayout) root.findViewById(R.id.fbLikes);
        if (likesButtons.getChildCount() > 0) {
            likesButtons.removeAllViews();
        }
        // sympathies buttons
        for (final BuyButtonData curButton : likes) {
            View btnView = Products.setBuyButton(likesButtons, curButton, getActivity(),
                    new Products.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buyProduct(id, mFrom);
                            Activity activity = getActivity();
                            if (activity instanceof PurchasesActivity) {
                                ((PurchasesActivity) activity).skipBonus();
                            }

                            CacheProfile.getOptions().topfaceOfferwallRedirect.setComplited(true);
                        }
                    }
            );
            if (btnView != null) {
                btnView.setTag(curButton);
            }
        }
    }

    private void initCoinsButtons(View root, LinkedList<BuyButtonData> coins) {
        if (coins == null || coins.isEmpty()) {
            setCoinsVisibility(false);
            return;
        }
        setCoinsVisibility(true);
        LinearLayout coinsButtonsContainer = (LinearLayout) root.findViewById(R.id.fbCoins);
        if (coinsButtonsContainer.getChildCount() > 0) {
            coinsButtonsContainer.removeAllViews();
        }
        for (final BuyButtonData curButton : coins) {
            View btnView = Products.setBuyButton(coinsButtonsContainer, curButton, getActivity(),
                    new Products.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buyProduct(id, mFrom);
                            Activity activity = getActivity();
                            if (activity instanceof PurchasesActivity) {
                                ((PurchasesActivity) activity).skipBonus();
                            }

                            CacheProfile.getOptions().topfaceOfferwallRedirect.setComplited(true);
                        }
                    }
            );
            if (btnView != null) {
                btnView.setTag(curButton);
            }
        }
        coinsButtonsContainer.requestLayout();
    }

    public String getFrom() {
        return mFrom;
    }
}