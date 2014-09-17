package com.topface.topface.requests.handlers;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.topface.topface.R;

/**
 * Handler for deferred purchases processing
 */
public class DeferredPurchaseHandler extends Handler {

    private View mBuyButton;
    private StartPurchase mStartPurchase;

    public static interface StartPurchase {
        void buy();
    }

    @Override
    public void handleMessage(Message msg) {
        stopWaiting();
        mStartPurchase.buy();
        super.handleMessage(msg);
    }

    public void setStartPurchase(StartPurchase startPurchase) {
        mStartPurchase = startPurchase;
    }

    public void setBuyButton(View view) {
        stopWaiting();
        mBuyButton = view;
    }

    public void startWaiting() {
        if (mBuyButton != null) {
            mBuyButton.findViewById(R.id.itText).setVisibility(View.INVISIBLE);
            mBuyButton.findViewById(R.id.marketWaiter).setVisibility(View.VISIBLE);
        }
    }

    public void stopWaiting() {
        if (mBuyButton != null) {
            mBuyButton.findViewById(R.id.itText).setVisibility(View.VISIBLE);
            mBuyButton.findViewById(R.id.marketWaiter).setVisibility(View.GONE);
        }
    }
}
