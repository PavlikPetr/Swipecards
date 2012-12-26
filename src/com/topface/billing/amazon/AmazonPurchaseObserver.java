package com.topface.billing.amazon;

import android.content.Context;
import android.os.Looper;
import com.amazon.inapp.purchasing.BasePurchasingObserver;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.Receipt;
import com.topface.billing.BillingListener;
import com.topface.billing.BillingSupportListener;
import com.topface.topface.App;
import com.topface.topface.requests.AmazonValidateRequest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

/**
 * Класс, обрабатывающий события покупки от Amazon
 */
public class AmazonPurchaseObserver extends BasePurchasingObserver {
    private final AmazonBillingDriver mDriver;
    private final Context mContext;

    public AmazonPurchaseObserver(Context context, AmazonBillingDriver amazonBillingDriver) {
        super(context);
        mContext = context;
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

        if (purchaseResponse != null && mDriver != null) {
            BillingListener listener = mDriver.getBillingListener();
            Debug.log(
                    String.format(
                            "Amazon In-APP Purchase request #%s for user #%s \n status: %s \n receipt: %s",
                            purchaseResponse.getRequestId(),
                            purchaseResponse.getUserId(),
                            purchaseResponse.getPurchaseRequestStatus(),
                            purchaseResponse.getReceipt()
                    )
            );

            handleCallbacks(purchaseResponse, listener);
        }
    }

    private void handleCallbacks(PurchaseResponse purchaseResponse, final BillingListener listener) {
        switch (purchaseResponse.getPurchaseRequestStatus()) {
            case SUCCESSFUL:
                //Добавляем запрос в очередь
                String userId = purchaseResponse.getUserId();
                Receipt receipt = purchaseResponse.getReceipt();
                if (receipt != null && receipt.getPurchaseToken() != null) {
                    String purchaseToken = receipt.getPurchaseToken();
                    validateRequest(listener, userId, purchaseToken, mContext);
                } else if (listener != null) {
                    listener.onError();
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

    public static void validateRequest(final BillingListener listener, String userId, String purchaseToken, final Context context) {
        //Добавляем запрос в очередь
        final String queueId = AmazonQueue.getInstance(context)
                .addPurchaseToQueue(userId, purchaseToken);

        new AmazonValidateRequest(userId, purchaseToken, context)
                .callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        AmazonQueue.getInstance(context).deleteQueueItem(queueId);
                        if (listener != null) {
                            listener.onPurchased();
                        }
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        //Если это ошибка оплаты от сервера, то удалям из очереди
                        if (
                                response.code == ApiResponse.INVALID_TRANSACTION ||
                                        response.code == ApiResponse.INVALID_PRODUCT
                                ) {

                            AmazonQueue.getInstance(context).deleteQueueItem(queueId);
                        }

                        if (listener != null) {
                            listener.onError();
                        }
                    }

                    @Override
                    public void always(ApiResponse response) {
                        super.always(response);
                        //После завершения запроса, проверяем, есть ли элементы в очереди, если есть отправляем их на сервер
                        Looper.prepare();
                        AmazonQueue.getInstance(App.getContext()).sendQueueItems();
                        Looper.loop();
                    }
                })
                .exec();
    }

}
