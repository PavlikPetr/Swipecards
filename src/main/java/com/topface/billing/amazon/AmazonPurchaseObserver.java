package com.topface.billing.amazon;

import android.content.Context;
import android.text.TextUtils;

import com.amazon.inapp.purchasing.BasePurchasingObserver;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.Receipt;
import com.topface.billing.BillingListener;
import com.topface.billing.BillingSupportListener;
import com.topface.billing.BillingUtils;
import com.topface.topface.App;
import com.topface.topface.requests.AmazonValidateRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
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
            listener.onSubscriptionSupported();
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
                String userId = purchaseResponse.getUserId();
                String sku = purchaseResponse.getReceipt().getSku();
                Receipt receipt = purchaseResponse.getReceipt();
                if (receipt != null && receipt.getPurchaseToken() != null) {
                    String purchaseToken = receipt.getPurchaseToken();
                    validateRequest(
                            listener,
                            null, //У нас пока нет id очереди
                            sku,
                            userId,
                            purchaseToken,
                            purchaseResponse.getRequestId(),
                            mContext
                    );
                } else if (listener != null) {
                    listener.onError();
                }

                break;

            case ALREADY_ENTITLED:
                //При попытке купить уже купленые товары просто отправлям коллбэк, не трогая сервер
                if (listener != null) {
                    listener.onPurchased(purchaseResponse.getReceipt().getSku());
                }
                break;

            default:
                //Все остальные статусы будут считаться ошибкой
                if (listener != null) {
                    listener.onError();
                }
        }
    }

    public static void validateRequest(final BillingListener listener,
                                       String queueId,
                                       final String sku, String userId, String purchaseToken, String requestId,
                                       final Context context) {
        //Добавляем запрос в очередь только если это не уже запрос из очереди
        final String queueNewId;
        if (TextUtils.isEmpty(queueId)) {
            queueNewId = AmazonQueue.getInstance(context)
                    .addPurchaseToQueue(sku, userId, purchaseToken, requestId);
        } else {
            queueNewId = queueId;
        }

        new AmazonValidateRequest(sku, userId, purchaseToken, requestId, context)
                .callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        AmazonQueue.getInstance(context).deleteQueueItem(queueNewId);
                        if (listener != null) {
                            listener.onPurchased(sku);
                        }
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        //Если это ошибка оплаты от сервера, то удалям из очереди
                        if (BillingUtils.isExceptedBillingError(codeError)) {
                            AmazonQueue.getInstance(context).deleteQueueItem(queueNewId);
                        }

                        if (listener != null) {
                            listener.onError();
                        }
                    }

                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        //После завершения запроса, проверяем, есть ли элементы в очереди, если есть отправляем их на сервер
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AmazonQueue.getInstance(App.getContext()).sendQueueItems();
                            }
                        }, BillingUtils.BILLING_QUEUE_CHECK_DELAY);
                    }
                })
                .exec();
    }

}
