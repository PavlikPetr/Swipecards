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
import android.widget.TextView;

import com.topface.billing.OpenIabFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.views.BuyButtonVer2;
import com.topface.topface.utils.CacheProfile;

import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class CoinsBuyingFragment extends OpenIabFragment {
    private LinkedList<View> purchaseButtons = new LinkedList<>();
    private TextView mResourceInfo;
    private String mResourceInfoText;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                getDataFromIntent(intent.getExtras());
            }
        }
    };

    private void getDataFromIntent(Bundle args) {
        if (args != null) {
            mFrom = args.getString(PurchasesConstants.ARG_TAG_SOURCE);
            if (args.containsKey(PurchasesConstants.ARG_RESOURCE_INFO_TEXT)) {
                mResourceInfoText = args.getString(PurchasesConstants.ARG_RESOURCE_INFO_TEXT);
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
        mResourceInfo = (TextView) root.findViewById(R.id.payReasonFragmentBuy);
        setResourceInfoText();
        initButtons(root);
        return root;
    }

    private void initButtons(View root) {
        LinearLayout likesButtons = (LinearLayout) root.findViewById(R.id.fbLikes);

        Products products = getProducts();
        //Если у нас нет продуктов, то не показываем кнопки
        if (products == null) {
            return;
        }
        if (products.likes.isEmpty() && products.coins.isEmpty()) {
            root.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        }
        // sympathies buttons
        for (final BuyButtonData curButton : products.likes) {
            View btnView = Products.setBuyButton(likesButtons, curButton, getActivity(),
                    new Products.BuyButtonClickListener() {
                        @Override
                        public void onClick(String id) {
                            buy(curButton);
                            Activity activity = getActivity();
                            if (activity instanceof PurchasesActivity) {
                                ((PurchasesActivity) activity).skipBonus();
                            }

                            CacheProfile.getOptions().topfaceOfferwallRedirect.setComplited(true);
                        }
                    }
            );
            if (btnView != null) {
                purchaseButtons.add(btnView);
                btnView.setTag(curButton);
            }
        }
        // coins buttons
        initCoinsButtons(root, products);
    }

    /////
    //////
    /////
    private ArrayList<String> getViewsType(Products products) {
        if (products != null && products.info != null && products.info.views != null && products.info.views.buyVip != null) {
            return products.info.views.buyVip;
        } else {
            ArrayList<String> defaultTypes = new ArrayList<>();
            defaultTypes.add(Products.VIEW_V2);
            return defaultTypes;
        }
    }

    private LinkedList<BuyButtonData> discardTrialProducts(List<BuyButtonData> products) {
        LinkedList<BuyButtonData> noTrialList = new LinkedList<>();
        for (BuyButtonData data : products) {
            if (data.trialPeriodInDays == 0) {
                noTrialList.add(data);
            }
        }
        return noTrialList;
    }

    private View getButtonView(ArrayList<String> viewsType, List<BuyButtonData> products, BuyButtonData buyBtn,
                               Context context, Products.BuyButtonClickListener listener) {
        if (viewsType.contains(Products.VIEW_V2)) {
            return getViewV2(products, buyBtn, listener, context);
        } else {
            return getViewV1(buyBtn, context, listener);
        }
    }

    private View getViewV1(BuyButtonData buyBtn,
                           Context context, Products.BuyButtonClickListener listener) {
        return Products.createBuyButtonLayout(context, buyBtn, listener);
    }

    private String getTotalPrice() {
        return "Total: 5$";
    }

    private String getDiscount() {
        return "20% for free";
    }

    private String getPricePerItem() {
        return "$1/month";
    }

    private int getIndex(LinkedList<BuyButtonData> products, BuyButtonData buyBtn) {
        for (int i = 0; i < products.size(); i++) {
            if (buyBtn.id.equals(products.get(i).id)) {
                return i;
            }
        }
        return -1;
    }

    private View getViewV2(List<BuyButtonData> products, final BuyButtonData buyBtn, final Products.BuyButtonClickListener listener, Context context) {
        String discount = null;
        String totalPrice = null;
        String pricePerItem = null;
        LinkedList<BuyButtonData> noTrialList = discardTrialProducts(products);
        BuyButtonVer2.BuyButtonBuilder builder = new BuyButtonVer2.BuyButtonBuilder().title(buyBtn.title).type(BuyButtonVer2.BUTTON_TYPE_BLUE).stickerType(BuyButtonVer2.STICKER_TYPE_NONE).onClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(buyBtn.id);
            }
        });
        int pos = getIndex(noTrialList, buyBtn);
        if (buyBtn.trialPeriodInDays == 0 && pos > 0) {
            // catch the second not trial product
            if (pos == 1) {
                builder.stickerType(BuyButtonVer2.STICKER_TYPE_POPULAR);
            }
            // catch the last not trial product
            if (pos == noTrialList.size() - 1) {
                builder.stickerType(BuyButtonVer2.STICKER_TYPE_BEST_VALUE);
                builder.type(BuyButtonVer2.BUTTON_TYPE_GREEN);
            }
            totalPrice = getTotalPrice();
            discount = getDiscount();
            pricePerItem = getPricePerItem();
        }
        return builder.discount(discount).totalPrice(totalPrice).pricePerItem(pricePerItem).build(context);
    }
    ////
    ///
    ///


    private void initCoinsButtons(View root, Products products) {
        if (products == null) {
            return;
        }
        boolean coinsMaskedExperiment = CacheProfile.getOptions().forceCoinsSubscriptions;
        List<BuyButtonData> coinsProducts = getCoinsProducts(products, coinsMaskedExperiment);
        root.findViewById(R.id.coins_title).setVisibility(
                coinsProducts.isEmpty() ? View.GONE : View.VISIBLE
        );
        LinearLayout coinsButtonsContainer = (LinearLayout) root.findViewById(R.id.fbCoins);
        if (coinsButtonsContainer.getChildCount() > 0) {
            coinsButtonsContainer.removeAllViews();
        }
        // coins items buttons also coinsSubscriptionsMasked buttons
        for (final BuyButtonData curButton : coinsProducts) {
//            View btnView = Products.setBuyButton(coinsButtonsContainer, curButton, getActivity(),
//                    new Products.BuyButtonClickListener() {
//                        @Override
//                        public void onClick(String id) {
//                            buy(curButton);
//                            Activity activity = getActivity();
//                            if (activity instanceof PurchasesActivity) {
//                                ((PurchasesActivity) activity).skipBonus();
//                            }
//
//                            CacheProfile.getOptions().topfaceOfferwallRedirect.setComplited(true);
//                        }
//                    }
//            );
            View btnView = Products.setBuyButton(coinsButtonsContainer, getButtonView(getViewsType(products), coinsProducts, curButton, getActivity(), new Products.BuyButtonClickListener() {
                @Override
                public void onClick(String id) {
                    buy(curButton);
                }
            }), curButton);
            if (btnView != null) {
                purchaseButtons.add(btnView);
                btnView.setTag(curButton);
            }
        }
        coinsButtonsContainer.requestLayout();
    }

    public String getFrom() {
        return mFrom;
    }

    @Override
    public void onInAppBillingSupported() {
        for (View btn : purchaseButtons) {
            btn.setEnabled(true);
        }
    }

    @Override
    public void onSubscriptionSupported() {
        //В этом типе фрагментов подписок нет
    }


    @Override
    public void onSubscriptionUnsupported() {
        //В этом типе фрагментов подписок нет
    }

    @Override
    public void onPurchased(Purchase product) {
        super.onPurchased(product);
        Debug.log("Purchased item with ID:" + product.getSku());
        final Products products = getProducts();
        if (products != null && products.isSubscription(product)) {
            App.sendProfileAndOptionsRequests(new SimpleApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    super.success(response);
                    if (isAdded()) {
                        initCoinsButtons(getView(), getProducts());
                    }
                }
            });
        }
    }

    protected abstract List<BuyButtonData> getCoinsProducts(Products products, boolean coinsMaskedExperiment);
}
