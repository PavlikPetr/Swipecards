package com.topface.billing.amazon;

import android.content.Context;
import com.amazon.inapp.purchasing.BasePurchasingObserver;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.topface.billing.BillingListener;
import com.topface.billing.BillingSupportListener;
import com.topface.topface.utils.Debug;

/**
 * Класс, обрабатывающий события покупки от Amazon
 */
public class AmazonPurchaseObserver extends BasePurchasingObserver {
    private final AmazonBillingDriver mDriver;

    public AmazonPurchaseObserver(Context context, AmazonBillingDriver amazonBillingDriver) {
        super(context);
        mDriver = amazonBillingDriver;
    }

    @Override
    public void onSdkAvailable(boolean b) {
        super.onSdkAvailable(b);
        BillingSupportListener listener = mDriver.getBillingSupportListener();
        if (listener != null) {
            //Если SDK доступно, запрашиваем id пользователя
            PurchasingManager.initiateGetUserIdRequest();

            //Если SDK доступно, то мы можем и подписываться и покупать
            listener.onInAppBillingSupported();
            listener.onSubscritionSupported();
        }
    }

    @Override
    public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
        super.onPurchaseResponse(purchaseResponse);

        if (purchaseResponse != null) {
            BillingListener listener = mDriver.getBillingListener();
            Debug.log(
                    String.format(
                            "Amazon In-APP Purchase request #%s for user #%s \n status: %s \n receipt: %s",
                            purchaseResponse.getRequestId(),
                            purchaseResponse.getUserId(),
                            purchaseResponse.getPurchaseRequestStatus().toString(),
                            purchaseResponse.getReceipt().toString()
                    )
            );

            handleCallbacks(purchaseResponse, listener);
        }
    }

    private void handleCallbacks(PurchaseResponse purchaseResponse, BillingListener listener) {
        switch (purchaseResponse.getPurchaseRequestStatus()) {
            case SUCCESSFUL:
                //TODO: Тут будет AmazonVerifyRequest, будем отправлять UserId и PurchaseToken

                //TODO: Не забыть, что нужно добавить очередь запросов

                //TODO: а вызов коллбэка не забыть сделать асинхронным
                if (listener != null) {
                    listener.onPurchased();
                }
                break;
            case ALREADY_ENTITLED:
                //При попытке купить уже купленые товары просто отправлям коллбэк, не трогая сервер
                if (listener != null) {
                    listener.onPurchased();
                }
                break;
            default:
                //Все остальные статусы будут считаться ошибкой
                if (listener != null) {
                    listener.onError();
                }
        }
    }

}
