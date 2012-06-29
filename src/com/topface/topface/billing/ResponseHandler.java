// Copyright 2010 Google Inc. All Rights Reserved.

package com.topface.topface.billing;

import com.topface.topface.billing.BillingService.RequestPurchase;
import com.topface.topface.billing.BillingService.RestoreTransactions;
import com.topface.topface.billing.Consts.PurchaseState;
import com.topface.topface.billing.Consts.ResponseCode;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ResponseHandler {
    // Data
    private static PurchaseObserver sPurchaseObserver;
    //---------------------------------------------------------------------------
    public static synchronized void register(PurchaseObserver observer) {
        sPurchaseObserver = observer;
    }
    //---------------------------------------------------------------------------
    public static synchronized void unregister(PurchaseObserver observer) {
        sPurchaseObserver = null;
    }
    //---------------------------------------------------------------------------
    public static void checkBillingSupportedResponse(boolean supported) {
        if (sPurchaseObserver != null)
            sPurchaseObserver.onBillingSupported(supported);
    }
    //---------------------------------------------------------------------------
    public static void buyPageIntentResponse(PendingIntent pendingIntent,Intent intent) {
        if (sPurchaseObserver == null)
            return;
        sPurchaseObserver.startBuyPageActivity(pendingIntent, intent);
    }
    //---------------------------------------------------------------------------
    public static void purchaseResponse(final PurchaseState purchaseState,final String data,final String signature) {
        new Thread(new Runnable() {
            public void run() {
                synchronized(ResponseHandler.class) {
                    if (sPurchaseObserver != null)
                        sPurchaseObserver.postPurchaseStateChange(purchaseState, data, signature);
                }
            }
        }).start();
    }
    //---------------------------------------------------------------------------
    public static void responseCodeReceived(Context context,RequestPurchase request,ResponseCode responseCode) {
        if (sPurchaseObserver != null)
            sPurchaseObserver.onRequestPurchaseResponse(request, responseCode);
    }
    //---------------------------------------------------------------------------
    public static void responseCodeReceived(Context context,RestoreTransactions request,ResponseCode responseCode) {
        if (sPurchaseObserver != null)
            sPurchaseObserver.onRestoreTransactionsResponse(request, responseCode);
    }
    //---------------------------------------------------------------------------
}
