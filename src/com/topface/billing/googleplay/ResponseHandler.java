// Copyright 2010 Google Inc. All Rights Reserved.

package com.topface.billing.googleplay;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.topface.billing.googleplay.BillingService.RequestPurchase;
import com.topface.billing.googleplay.BillingService.RestoreTransactions;
import com.topface.billing.googleplay.Consts.PurchaseState;
import com.topface.billing.googleplay.Consts.ResponseCode;
import com.topface.topface.App;
import com.topface.topface.data.Verify;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.VerifyRequest;
import com.topface.topface.utils.CacheProfile;

/**
 * This class contains the methods that handle responses from Android Market.  The
 * implementation of these methods is specific to a particular application.
 * The methods in this example update the database and, if the main application
 * has registered a {@llink PurchaseObserver}, will also update the UI.  An
 * application might also want to forward some responses on to its own server,
 * and that could be done here (in a background thread) but this example does
 * not do that.
 * <p/>
 * You should modify and obfuscate this code before using it.
 */
public class ResponseHandler {
    private static final String TAG = "ResponseHandler";

    /**
     * This is a static instance of {@link PurchaseObserver} that the
     * application creates and registers with this class. The PurchaseObserver
     * is used for updating the UI if the UI is visible.
     */
    private static PurchaseObserver sPurchaseObserver;

    /**
     * Registers an observer that updates the UI.
     *
     * @param observer the observer to register
     */
    public static synchronized void register(PurchaseObserver observer) {
        sPurchaseObserver = observer;
    }

    /**
     * Unregisters a previously registered observer.
     *
     * @param observer the previously registered observer.
     */
    public static synchronized void unregister(PurchaseObserver observer) {
        sPurchaseObserver = null;
    }

    /**
     * Notifies the application of the availability of the MarketBillingService.
     * This method is called in response to the application calling
     * {@link BillingService#checkBillingSupported()}.
     *
     * @param supported true if in-app billing is supported.
     */
    public static void checkBillingSupportedResponse(boolean supported, String type) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onBillingSupported(supported, type);
        }
    }

    /**
     * Starts a new activity for the user to buy an item for sale. This method
     * forwards the intent on to the PurchaseObserver (if it exists) because
     * we need to start the activity on the activity stack of the application.
     *
     * @param pendingIntent a PendingIntent that we received from Android Market that
     *                      will create the new buy page activity
     * @param intent        an intent containing a request id in an extra field that
     *                      will be passed to the buy page activity when it is created
     */
    public static void buyPageIntentResponse(PendingIntent pendingIntent, Intent intent) {
        if (sPurchaseObserver == null) {
            if (Consts.DEBUG) {
                Log.d(TAG, "UI is not running");
            }
            return;
        }
        sPurchaseObserver.startBuyPageActivity(pendingIntent, intent);
    }

    /**
     * Notifies the application of purchase state changes. The application
     * can offer an item for sale to the user via
     * {@link BillingService#requestPurchase(String)}. The BillingService
     * calls this method after it gets the response. Another way this method
     * can be called is if the user bought something on another device running
     * this same app. Then Android Market notifies the other devices that
     * the user has purchased an item, in which case the BillingService will
     * also call this method. Finally, this method can be called if the item
     * was refunded.
     *
     * @param purchaseState    the state of the purchase request (PURCHASED,
     *                         CANCELED, or REFUNDED)
     * @param productId        a string identifying a product for sale
     * @param orderId          a string identifying the order
     * @param purchaseTime     the time the product was purchased, in milliseconds
     *                         since the epoch (Jan 1, 1970)
     * @param developerPayload the developer provided "payload" associated with
     * @param signedData       signed order data
     * @param signature        signature for check data
     */
    public static void purchaseResponse(
            final Context context, final PurchaseState purchaseState, final String productId,
            final String orderId, final long purchaseTime, final String developerPayload, final String signedData, final String signature) {

        //Отправляем проверку на сервер
        if (purchaseState == PurchaseState.PURCHASED) {
            //Перед отправкой добаляем в очередь
            String queueId = GooglePlayV2Queue.getInstance(context).addPurchaseToQueue(signedData, signature);
            verifyPurchase(context, signedData, signature, queueId);
        }

        if (sPurchaseObserver != null) {
            sPurchaseObserver.postPurchaseStateChange(
                    purchaseState, productId, 1, purchaseTime, developerPayload, signedData, signature);
        }
    }

    /**
     * This is called when we receive a response code from Android Market for a
     * RequestPurchase request that we made.  This is used for reporting various
     * errors and also for acknowledging that an order was sent successfully to
     * the server. This is NOT used for any purchase state changes. All
     * purchase state changes are received in the {@link BillingReceiver} and
     * are handled in {@link Security#verifyPurchase(String, String)}.
     *
     * @param context      the context
     * @param request      the RequestPurchase request for which we received a
     *                     response code
     * @param responseCode a response code from Market to indicate the state
     *                     of the request
     */
    public static void responseCodeReceived(Context context, RequestPurchase request,
                                            ResponseCode responseCode) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRequestPurchaseResponse(request, responseCode);
        }
    }

    /**
     * This is called when we receive a response code from Android Market for a
     * RestoreTransactions request.
     *
     * @param context      the context
     * @param request      the RestoreTransactions request for which we received a
     *                     response code
     * @param responseCode a response code from Market to indicate the state
     *                     of the request
     */
    public static void responseCodeReceived(Context context, RestoreTransactions request,
                                            ResponseCode responseCode) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRestoreTransactionsResponse(request, responseCode);
        }
    }

    /**
     * Проверка платежа на сервере
     * Этот метод может вызываться как при покупке, так и при разборе очереди,
     *
     * @param context   текущий контекст
     * @param data      данные платежа
     * @param signature подпись данных платежа
     * @param queueId   id покуки в очереди запросов
     */
    public static void verifyPurchase(final Context context, final String data, final String signature, final String queueId) {
        // Отправлем заказ на сервер
        final VerifyRequest verifyRequest = new VerifyRequest(context);
        verifyRequest.data = data;
        verifyRequest.sandbox = true;
        verifyRequest.signature = signature;
        verifyRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                //Удаляем запрос из очереди запросов
                GooglePlayV2Queue.getInstance(context).deleteQueueItem(queueId);
                Verify verify = Verify.parse(response);
                CacheProfile.power = verify.power;
                CacheProfile.money = verify.money;
                CacheProfile.premium = verify.premium;
                //Оповещаем интерфейс о том, что элемент удачно куплен
                if (sPurchaseObserver != null) {
                    sPurchaseObserver.postVerify(response);
                }
            }

            @Override
            public void fail(int codeError, final ApiResponse response) {
                //Если сервер определил как не верный или поддельный, или мы не знаем такой продукт, удаляем его из очереди
                if (
                        codeError == ApiResponse.INVALID_PRODUCT ||
                                codeError == ApiResponse.INVALID_TRANSACTION ||
                                codeError == ApiResponse.INVALID_FORMAT ||
                                codeError == ApiResponse.UNVERIFIED_SIGNATURE

                        ) {

                    GooglePlayV2Queue.getInstance(context).deleteQueueItem(queueId);
                }
                //В случае ошибки не забываем оповестить об этом
                if (sPurchaseObserver != null) {
                    sPurchaseObserver.postVerify(response);
                }
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
                //После завершения запроса, проверяем, есть ли элементы в очереди, если есть отправляем их на сервер
                GooglePlayV2Queue.getInstance(App.getContext()).sendQueueItems();
            }
        }).exec();
    }

}
