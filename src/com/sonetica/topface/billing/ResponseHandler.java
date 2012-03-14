// Copyright 2010 Google Inc. All Rights Reserved.

package com.sonetica.topface.billing;

import com.sonetica.topface.billing.BillingService.RequestPurchase;
import com.sonetica.topface.billing.BillingService.RestoreTransactions;
import com.sonetica.topface.billing.Consts.PurchaseState;
import com.sonetica.topface.billing.Consts.ResponseCode;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ResponseHandler {
    private static final String TAG = "ResponseHandler";
    private static PurchaseObserver sPurchaseObserver;
    public static synchronized void register(PurchaseObserver observer) {
        sPurchaseObserver = observer;
    }
    public static synchronized void unregister(PurchaseObserver observer) {
        sPurchaseObserver = null;
    }
    public static void checkBillingSupportedResponse(boolean supported) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onBillingSupported(supported);
        }
    }
    public static void buyPageIntentResponse(PendingIntent pendingIntent, Intent intent) {
        if (sPurchaseObserver == null) {
            if (Consts.DEBUG) {
                Log.d(TAG, "UI is not running");
            }
            return;
        }
        sPurchaseObserver.startBuyPageActivity(pendingIntent, intent);
    }
    public static void purchaseResponse(
            final Context context, final PurchaseState purchaseState, final String productId,
            final String orderId, final long purchaseTime, final String developerPayload) {

        new Thread(new Runnable() {
            public void run() {
              int quantity = 1;
                synchronized(ResponseHandler.class) {
                    if (sPurchaseObserver != null) {
                        sPurchaseObserver.postPurchaseStateChange(
                                purchaseState, productId, quantity, purchaseTime, developerPayload);
                    }
                }
            }
        }).start();
    }
    public static void responseCodeReceived(Context context, RequestPurchase request,
            ResponseCode responseCode) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRequestPurchaseResponse(request, responseCode);
        }
    }

    public static void responseCodeReceived(Context context, RestoreTransactions request,
            ResponseCode responseCode) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRestoreTransactionsResponse(request, responseCode);
        }
    }
}
