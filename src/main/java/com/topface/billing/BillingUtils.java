package com.topface.billing;

import android.os.Handler;
import android.text.TextUtils;
import com.topface.billing.amazon.AmazonQueue;
import com.topface.billing.googleplay.GooglePlayV2Queue;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.Utils;

public class BillingUtils {
    public static final int BILLING_QUEUE_CHECK_DELAY = 1500;

    /**
     * Метод, проверяющий, является ли указаный код ошибки "ожидаемым",
     * т.е. это значит сервер обработал запрос и вернул ошибку, и следует удалить запрос из очереди
     * если же это какая либо другая ошибка, то мы будем до победного отправлять ее на сервер
     */
    public static boolean isExceptedBillingError(int responseCode) {
        boolean isExcepted;

        switch (responseCode) {
            case ErrorCodes.INVALID_TRANSACTION:
            case ErrorCodes.INVALID_PRODUCT:
            case ErrorCodes.INCORRECT_VALUE:
            case ErrorCodes.INVALID_PURCHASE_TOKEN:
            case ErrorCodes.INVALID_FORMAT:
            case ErrorCodes.UNVERIFIED_SIGNATURE:
            case ErrorCodes.MISSING_REQUIRE_PARAMETER:
                isExcepted = true;
                break;
            default:
                isExcepted = false;
        }

        return isExcepted;
    }

    /**
     * Пробуем разобрать очередь запросов
     */
    public static void sendQueueItems() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String buildType = Utils.getBuildType();
                String googlePlay = App.getContext().getString(R.string.build_google_play_v2);
                String amazon = App.getContext().getString(R.string.build_amazon);

                if (TextUtils.equals(googlePlay, buildType)) {
                    GooglePlayV2Queue.getInstance(App.getContext()).sendQueueItems();

                } else if (TextUtils.equals(amazon, buildType)) {
                    AmazonQueue.getInstance(App.getContext()).sendQueueItems();
                }

            }
        }, BILLING_QUEUE_CHECK_DELAY);
    }
}
