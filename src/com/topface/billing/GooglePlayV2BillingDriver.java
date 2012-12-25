package com.topface.billing;

import android.app.Activity;
import android.os.Handler;
import com.topface.billing.googleplay.BillingService;
import com.topface.billing.googleplay.Consts;
import com.topface.billing.googleplay.PurchaseObserver;
import com.topface.billing.googleplay.ResponseHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/**
 * Драйвер In-App покупок в Google Play версии 2
 */
public class GooglePlayV2BillingDriver extends BillingDriver {


    private final BillingService mBillingService;

    public GooglePlayV2BillingDriver(Activity activity, BillingListener listener) {
        super(activity, listener);

        mBillingService = new BillingService();
        mBillingService.setContext(activity);

        ResponseHandler.register(new GooglePlayPurchaseObserver(new Handler()));

    }

    @Override
    protected void checkBillingSupport(BillingSupportListener listener) {
        //Проверяем, какие методы покупки доступны и вызываем соотвесвующие коллбэки
        if (mBillingService.checkBillingSupported(Consts.ITEM_TYPE_INAPP)) {
            listener.onInAppBillingSupported();
        } else {
            listener.onInAppBillingUnsupported();
        }

        if (mBillingService.checkBillingSupported(Consts.ITEM_TYPE_SUBSCRIPTION)) {
            listener.onSubscritionSupported();
        } else {
            listener.onSubscritionUnsupported();
        }
    }

    @Override
    public void onDestroy() {
        mBillingService.unbind();
    }

    @Override
    public void buyItem(String itemId) {
        mBillingService.requestPurchase(itemId, Consts.ITEM_TYPE_INAPP, null);
    }

    @Override
    public void buySubscriotion(String subscriptionId) {
        mBillingService.requestPurchase(subscriptionId, Consts.ITEM_TYPE_SUBSCRIPTION, null);
    }

    @Override
    public String getDriverName() {
        return "Google Play version 2";
    }

    private class GooglePlayPurchaseObserver extends PurchaseObserver {
        public GooglePlayPurchaseObserver(Handler handler) {
            super(getActivity(), handler);
        }

        @Override
        public void onBillingSupported(boolean supported, String type) {
        }

        /**
         * В этот коллбэк приходят все изменения статуса платежа, включая успешную покупку
         * Но мы не используем его, т.к. дополнительно отправляем данные на сервер, см {@link PurchaseObserver#onVerifyResponse}
         *
         * @param purchaseState    the purchase state of the item
         * @param itemId           a string identifying the item (the "SKU")
         * @param quantity         the current quantity of this item after the purchase
         * @param purchaseTime     the time the product was purchased, in
         * @param developerPayload дополнительные данные покупки
         * @param signedData       подписанные данные
         * @param signature        подпись данных
         */
        @Override
        public void onPurchaseStateChange(Consts.PurchaseState purchaseState, String itemId,
                                          int quantity, long purchaseTime, String developerPayload,
                                          String signedData, String signature) {
        }

        /**
         * Этот коллбэк просто оповещает нас об обмене запросами с сервером и их результаты,
         * сами резултаты покупок сюда не приходят, для этого есть onPurchaseStateChange и onVerifyResponse
         *
         * @param request      запрос для google play
         * @param responseCode код ответа
         */
        @Override
        public void onRequestPurchaseResponse(BillingService.RequestPurchase request,
                                              Consts.ResponseCode responseCode) {
            if (mBillingListener != null) {
                //Если пользователь сам отменил, то вызываем коллбэк onCancel();
                if (responseCode == Consts.ResponseCode.RESULT_USER_CANCELED) {
                    Debug.log("Billing: onCancel");
                    mBillingListener.onCancel();
                } else if (responseCode != Consts.ResponseCode.RESULT_OK) {
                    //Если это другая ошибка, то возвращаем соответсвующий коллбэк
                    Debug.log("Billing: onError " + responseCode);
                    mBillingListener.onError();
                }
            }
        }

        /**
         * В этом коллэке можно восстанавливать уже произведенные транзакции, но наш тип покупок этого не поддреживает
         * да и нам это пока не нужно
         *
         * @param request      запрос на восстанвоаление платежа
         * @param responseCode результат запроса
         */
        @Override
        public void onRestoreTransactionsResponse(BillingService.RestoreTransactions request,
                                                  Consts.ResponseCode responseCode) {
        }

        @Override
        public void onVerifyResponse(ApiResponse response) {
            Debug.log(String.format("VerifyResponse: #%d:\n%s", response.code, response.jsonResult));
            if (mBillingListener != null) {
                if (response.code == ApiResponse.RESULT_OK) {
                    Debug.log("Billing: onPurchased");
                    mBillingListener.onPurchased();
                } else {
                    Debug.log("Billing: onError");
                    mBillingListener.onError();
                }
            }
        }

    }

}
