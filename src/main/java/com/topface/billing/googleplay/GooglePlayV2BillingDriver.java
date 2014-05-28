package com.topface.billing.googleplay;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import com.topface.billing.BillingDriver;
import com.topface.billing.BillingListener;
import com.topface.billing.BillingSupportListener;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;

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
    public void onStart() {
        checkBillingSupport(getBillingSupportListener());
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onStop() {
    }

    protected void checkBillingSupport(BillingSupportListener listener) {
        if (listener != null) {
            //Проверяем, какие методы покупки доступны и вызываем соотвесвующие коллбэки
            if (mBillingService.checkBillingSupported(Consts.ITEM_TYPE_INAPP)) {
                listener.onInAppBillingSupported();
            } else {
                listener.onInAppBillingUnsupported();
                Toast.makeText(App.getContext(), R.string.buy_play_market_not_available, Toast.LENGTH_SHORT).show();
            }

            if (mBillingService.checkBillingSupported(Consts.ITEM_TYPE_SUBSCRIPTION)) {
                listener.onSubscriptionSupported();
            } else {
                listener.onSubscriptionUnsupported();
            }
        }
    }

    @Override
    public void onDestroy() {
        mBillingService.unbind();
    }

    @Override
    public void buyItem(String itemId) {
        //Если это тестовые покупки, то подменяем id товара
        itemId = setTestProductId(itemId);

        mBillingService.requestPurchase(itemId, Consts.ITEM_TYPE_INAPP, getDeveloperPayload());
    }

    @Override
    public void buySubscription(String subscriptionId) {
        //Если включены тестовые покупки, то покупаем не подписку, т.к. нет тестовых ключей подписок
        if (isTestPurchasesEnabled()) {
            buyItem(subscriptionId);
        } else {
            mBillingService.requestPurchase(
                    subscriptionId,
                    Consts.ITEM_TYPE_SUBSCRIPTION,
                    getDeveloperPayload()
            );
        }
    }

    private String setTestProductId(String itemId) {
        //Тестовые покупки
        if (isTestPurchasesEnabled()) {
            //Для проброса данных о тестовой покупке просто сохраняем оригинальный id продукта
            BillingDriver.setProductIdForTestPayment(itemId);
            //Заменяем id продукта на тестовый, который позволит нам совершить покупку через GP
            itemId = "android.test.purchased";
        } else {
            BillingDriver.setProductIdForTestPayment(null);
        }
        return itemId;
    }

    private String getDeveloperPayload() {
        return "{\"id\": " + CacheProfile.uid + "}";
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
            BillingListener listener = getBillingListener();
            if (listener != null) {
                //Если пользователь сам отменил, то вызываем коллбэк onCancel();
                if (responseCode == Consts.ResponseCode.RESULT_USER_CANCELED) {
                    Debug.log("Billing: onCancel");
                    listener.onCancel();
                } else if (responseCode != Consts.ResponseCode.RESULT_OK) {
                    //Если это другая ошибка, то возвращаем соответсвующий коллбэк
                    Debug.log("Billing: onError " + responseCode);
                    listener.onError();
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
        public void onVerifyResponse(IApiResponse response, String productId) {
            Debug.log(String.format("VerifyResponse: #%d:\n%s", response.getResultCode(), response.getJsonResult()));
            BillingListener listener = getBillingListener();
            if (listener != null) {
                if (response.isCompleted()) {
                    Debug.log("Billing: onPurchased");
                    listener.onPurchased(productId);
                } else {
                    Debug.log("Billing: onError");
                    listener.onError();
                }
            }
        }

    }

    @Override
    public boolean isTestPurchasesAvailable() {
        return CacheProfile.isEditor();
    }
}
