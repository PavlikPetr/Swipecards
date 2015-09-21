package com.topface.topface.ui.fragments.buy;



import android.support.v4.app.Fragment;

import com.topface.topface.ui.views.ITransparentMarketFragmentRunner;


public class TransparentMarketFragment extends Fragment implements ITransparentMarketFragmentRunner{

    public static TransparentMarketFragment newInstance(String skuId, boolean isSubscription, String from) {
        return  new TransparentMarketFragment();
    }

    @Override
    public void setOnPurchaseCompleteAction(onPurchaseActions purchaseCompliteAction) {}


    public interface onPurchaseActions {

        void onPurchaseSuccess();

        void onPopupClosed();
    }

}
