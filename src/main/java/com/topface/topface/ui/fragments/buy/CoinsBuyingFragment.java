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
import com.topface.statistics.processor.utils.RxUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BuyButtonData;
import com.topface.topface.data.Products;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.external_libs.ironSource.IronSourceManager;
import com.topface.topface.ui.external_libs.ironSource.IronSourceOfferwallEvent;
import com.topface.topface.ui.views.BuyButtonVer1;

import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.LinkedList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class CoinsBuyingFragment extends OpenIabFragment {
    private LinkedList<View> purchaseButtons = new LinkedList<>();
    private TextView mResourceInfo;
    private Boolean mIsNeedOfferwalls = !App.get().getOptions()
            .getOfferwallWithPlaces().getPurchaseScreen().isEmpty()
            && App.get().getOptions().getOfferwallWithPlaces()
            .getName().equalsIgnoreCase(IronSourceManager.NAME);

    private IronSourceManager mIronSourceManager = App.getAppComponent().ironSourceManager();
    private Boolean mIronSrcAvailable = false;
    private BuyButtonVer1 coinsOfferwallBtn, sympOfferwallBtn;
    private String mResourceInfoText;
    private Subscription mIronsrcSubscription;
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
        mIronsrcSubscription = mIronSourceManager.getOfferwallObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new com.topface.topface.utils.rx.RxUtils.ShortSubscription<IronSourceOfferwallEvent>() {
                    @Override
                    public void onNext(IronSourceOfferwallEvent type) {
                        super.onNext(type);
                        long OfferwallType = type.getType();
                        if (OfferwallType == IronSourceOfferwallEvent.OFFERWALL_CLOSED || OfferwallType == IronSourceOfferwallEvent.OFFERWALL_OPENED) {
                            if (sympOfferwallBtn != null) {
                                sympOfferwallBtn.stopWaiting();
                            }
                            if (coinsOfferwallBtn != null) {
                                coinsOfferwallBtn.stopWaiting();
                            }
                        }
                    }
                });
        getDataFromIntent(getArguments());
        if (mIsNeedOfferwalls) {
            mIronSourceManager.initSdk(getActivity());
        }
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
    public void onDestroy() {
        super.onDestroy();
        RxUtils.safeUnsubscribe(mIronsrcSubscription);
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
        List<BuyButtonData> availableLikesButtons = getAvailableButtons(products.likes);
        List<BuyButtonData> availableCoinsButtons = getAvailableButtons(products.coins);
        if (availableLikesButtons.isEmpty() && availableCoinsButtons.isEmpty()) {
            root.findViewById(R.id.fbBuyingDisabled).setVisibility(View.VISIBLE);
        }
        // sympathies buttons
        purchaseButtons.addAll(new PurchaseButtonList().getButtonsListView(likesButtons, availableLikesButtons, App.getContext(), new PurchaseButtonList.BuyButtonClickListener() {
            @Override
            public void onClick(String id, BuyButtonData btnData) {
                buy(btnData);
                Activity activity = getActivity();
                if (activity instanceof PurchasesActivity) {
                    ((PurchasesActivity) activity).skipBonus();
                }

                App.from(getActivity()).getOptions().topfaceOfferwallRedirect.setComplited(true);
            }
        }));
        // coins buttons
        initCoinsButtons(root, products);
        // offerwall button
        if (mIsNeedOfferwalls) {
            initOfferwallButton(root);
        }
    }

    private void initCoinsButtons(View root, Products products) {
        if (products == null) {
            return;
        }
        boolean coinsMaskedExperiment = App.from(getActivity()).getOptions().forceCoinsSubscriptions;
        List<BuyButtonData> coinsProducts = getAvailableButtons(getCoinsProducts(products, coinsMaskedExperiment));
        root.findViewById(R.id.coins_title).setVisibility(
                coinsProducts.isEmpty() ? View.GONE : View.VISIBLE
        );
        LinearLayout coinsButtonsContainer = (LinearLayout) root.findViewById(R.id.fbCoins);
        if (coinsButtonsContainer.getChildCount() > 0) {
            coinsButtonsContainer.removeAllViews();
        }
        // coins items buttons also coinsSubscriptionsMasked buttons
        purchaseButtons.addAll(new PurchaseButtonList().getButtonsListView(coinsButtonsContainer, coinsProducts, App.getContext(), new PurchaseButtonList.BuyButtonClickListener() {
            @Override
            public void onClick(String id, BuyButtonData btnData) {
                buy(btnData);
                Activity activity = getActivity();
                if (activity instanceof PurchasesActivity) {
                    ((PurchasesActivity) activity).skipBonus();
                }

                App.from(getActivity()).getOptions().topfaceOfferwallRedirect.setComplited(true);
            }
        }));
        coinsButtonsContainer.requestLayout();
    }

    private void initOfferwallButton(View root) {
        if (App.get().getOptions().getOfferwallWithPlaces().getPurchaseScreen().contains(IronSourceManager.SYMPATHIES_OFFERWALL)) {
            final LinearLayout sympContainer = (LinearLayout) root.findViewById(R.id.fbLikes);
            sympOfferwallBtn = initButton(IronSourceManager.SYMPATHIES_OFFERWALL);
            sympContainer.addView(sympOfferwallBtn);
        }
        if (App.get().getOptions().getOfferwallWithPlaces().getPurchaseScreen().contains(IronSourceManager.COINS_OFFERWALL)) {
            final LinearLayout coinsContainer = (LinearLayout) root.findViewById(R.id.fbCoins);
            coinsOfferwallBtn = initButton(IronSourceManager.COINS_OFFERWALL);
            coinsContainer.addView(coinsOfferwallBtn);
        }
    }

    private BuyButtonVer1 initButton(String ironSrcType) {
        final BuyButtonVer1 offerwallBtn;
        offerwallBtn = new BuyButtonVer1.BuyButtonBuilder().discount(false)
                .tag(ironSrcType)
                .showType(3).title(getResources().getString(R.string.get_free))
                .onClick(null).build(getContext());
        offerwallBtn.setTag(ironSrcType);
        offerwallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIronSourceManager.emmitNewState(IronSourceOfferwallEvent.Companion.getOnOfferwallCall());
                mIronSourceManager.showOfferwallByType(String.valueOf(offerwallBtn.getTag()));
                offerwallBtn.startWaiting();
            }
        });
        return offerwallBtn;
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
