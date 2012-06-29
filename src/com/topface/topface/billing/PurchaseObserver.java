// Copyright 2010 Google Inc. All Rights Reserved.

package com.topface.topface.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.util.Log;
import java.lang.reflect.Method;
import com.topface.topface.billing.BillingService.RequestPurchase;
import com.topface.topface.billing.BillingService.RestoreTransactions;
import com.topface.topface.billing.Consts.PurchaseState;
import com.topface.topface.billing.Consts.ResponseCode;

public abstract class PurchaseObserver {
    // Data
    private static final String TAG = "PurchaseObserver";
    private final Activity mActivity;
    private final Handler mHandler;
    private Method mStartIntentSender;
    private Object[] mStartIntentSenderArgs = new Object[5];

    @SuppressWarnings("rawtypes")
    private static final Class[] START_INTENT_SENDER_SIG = new Class[]{IntentSender.class,Intent.class,int.class,int.class,int.class};
    //---------------------------------------------------------------------------
    public PurchaseObserver(Activity activity,Handler handler) {
        mActivity = activity;
        mHandler = handler;
        initCompatibilityLayer();
    }
    //---------------------------------------------------------------------------
    public abstract void onBillingSupported(boolean supported);
    public abstract void onPurchaseStateChange(PurchaseState purchaseState,String data,String signature);
    //public abstract void onPurchaseStateChange(PurchaseState purchaseState,String itemId,int quantity,long purchaseTime,String developerPayload);
    public abstract void onRequestPurchaseResponse(RequestPurchase request,ResponseCode responseCode);
    public abstract void onRestoreTransactionsResponse(RestoreTransactions request,ResponseCode responseCode);

    private void initCompatibilityLayer() {
        try {
            mStartIntentSender = mActivity.getClass().getMethod("startIntentSender", START_INTENT_SENDER_SIG);
        } catch(SecurityException e) {
            mStartIntentSender = null;
        } catch(NoSuchMethodException e) {
            mStartIntentSender = null;
        }
    }
    //---------------------------------------------------------------------------
    void startBuyPageActivity(PendingIntent pendingIntent,Intent intent) {
        if (mStartIntentSender != null) {
            try {
                mStartIntentSenderArgs[0] = pendingIntent.getIntentSender();
                mStartIntentSenderArgs[1] = intent;
                mStartIntentSenderArgs[2] = Integer.valueOf(0);
                mStartIntentSenderArgs[3] = Integer.valueOf(0);
                mStartIntentSenderArgs[4] = Integer.valueOf(0);
                mStartIntentSender.invoke(mActivity, mStartIntentSenderArgs);
            } catch(Exception e) {
                Log.e(TAG, "error starting activity", e);
            }
        } else {
            try {
                pendingIntent.send(mActivity, 0 /* code */, intent);
            } catch(CanceledException e) {
                Log.e(TAG, "error starting activity", e);
            }
        }
    }
    //---------------------------------------------------------------------------
    void postPurchaseStateChange(final PurchaseState purchaseState,final String data,final String signature) {
        mHandler.post(new Runnable() {
            public void run() {
                onPurchaseStateChange(purchaseState, data, signature);
            }
        });
    }
    //---------------------------------------------------------------------------
}
