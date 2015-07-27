package com.topface.topface.ui.fragments.buy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ifree.monetize.core.PurchaseListener;
import com.ifree.monetize.core.PurchaseResponse;
import com.topface.billing.IFreePurchases;

import static com.topface.topface.ui.fragments.buy.PurchasesConstants.ARG_TAG_SOURCE;


public class TransparentMarketFragment extends IFreePurchases implements PurchaseListener {

    public final static String PORDUCT_ID = "product_id";
    public final static String IS_SUBSCRIPTION = "is_subscription";

    private onPurchaseActions mPurchaseActions;
    private String mSubscriptionId;
    private boolean mIsSubscription;
    private boolean isNeedCloseFragment = false;
    private String mFrom;

    public static TransparentMarketFragment newInstance(String skuId, boolean isSubscription, String from) {
        final TransparentMarketFragment fragment = new TransparentMarketFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TransparentMarketFragment.PORDUCT_ID, skuId);
        bundle.putString(ARG_TAG_SOURCE, from);
        bundle.putBoolean(TransparentMarketFragment.IS_SUBSCRIPTION, isSubscription);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPurchaseListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (null != bundle) {
            if (getArguments().containsKey(PORDUCT_ID)) {
                mSubscriptionId = getArguments().getString(PORDUCT_ID, "");
            }
            if (getArguments().containsKey(IS_SUBSCRIPTION)) {
                mIsSubscription = getArguments().getBoolean(IS_SUBSCRIPTION);
            }
            if (getArguments().containsKey(ARG_TAG_SOURCE)) {
                mFrom = getArguments().getString(ARG_TAG_SOURCE, "");
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onLibraryInitialised() {
        super.onLibraryInitialised();
        String productId = null;
        try {
            productId = getMonetization().getPriceTariffGroup(mSubscriptionId);
        } catch (Exception ignored) {
        }
        if (productId != null) {
            buyProduct(mSubscriptionId, mFrom);
        }
    }

    public void setOnPurchaseCompleteAction(onPurchaseActions purchaseCompliteAction) {
        mPurchaseActions = purchaseCompliteAction;
    }

    @Override
    public void onPurchaseEventReceive(PurchaseResponse purchaseResponse) {
        if (mPurchaseActions != null) {
            mPurchaseActions.onPurchaseSuccess();
        }
    }

    public interface onPurchaseActions {

        void onPurchaseSuccess();

        void onPopupClosed();
    }
}
