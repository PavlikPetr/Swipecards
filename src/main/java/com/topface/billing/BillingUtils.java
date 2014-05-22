package com.topface.billing;

import android.content.Context;
import android.os.Handler;

import com.topface.billing.googleplay.GooglePlayV2Queue;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.requests.handlers.ErrorCodes;

public class BillingUtils {
    public static final int BILLING_QUEUE_CHECK_DELAY = 1500;

    /**
     * Метод, проверяющий, является ли указаный код ошибки "ожидаемым",
     * т.е. это значит сервер обработал запрос и вернул ошибку, и следует удалить запрос из очереди
     * если же это какая либо другая ошибка, то мы будем до победного отправлять ее на сервер
     */
    public static boolean isExceptedBillingError(int responseCode) {
        switch (responseCode) {
            case ErrorCodes.INVALID_TRANSACTION:
            case ErrorCodes.INVALID_PRODUCT:
            case ErrorCodes.INCORRECT_VALUE:
            case ErrorCodes.INVALID_PURCHASE_TOKEN:
            case ErrorCodes.INVALID_FORMAT:
            case ErrorCodes.UNVERIFIED_SIGNATURE:
            case ErrorCodes.MISSING_REQUIRE_PARAMETER:
                return true;
            default:
                return false;
        }
    }

    /**
     * Пробуем разобрать очередь запросов
     */
    public static void sendQueueItems() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (BuildConfig.BILLING_TYPE) {
                    case AMAZON:
                        try {
                            //мы получаем амазоновскую очередь данамически
                            Class queueClass = Class.forName("com.topface.billing.amazon.AmazonQueue");
                            //noinspection unchecked
                            Object queue = queueClass
                                    .getMethod("getInstance", Context.class)
                                    .invoke(null, App.getContext());
                            queue.getClass().getMethod("sendQueueItems").invoke(queue);
                        } catch (Exception e) {
                            Debug.error("Amazon library not found", e);
                        }
                        break;
                    case GOOGLE_PLAY:
                    default:
                        GooglePlayV2Queue.getInstance(App.getContext()).sendQueueItems();
                }
            }
        }, BILLING_QUEUE_CHECK_DELAY);
    }
}
