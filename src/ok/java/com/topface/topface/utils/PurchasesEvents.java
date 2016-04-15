package com.topface.topface.utils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.ReportPaymentData;
import com.topface.topface.statistics.ReportPaymentStatistics;
import com.topface.topface.utils.social.OkAuthorizer;

import rx.functions.Action1;

/**
 * Created by ppavlik on 05.04.16.
 * methods for purchases state
 */
public class PurchasesEvents {

    public static void purchaseSuccess(int productsCount, String productType, String productId, String currencyCode, double price, String transactionId) {
        new ReportPaymentRequest(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()), transactionId, price, currencyCode).getObservable().subscribe(new Action1<ReportPaymentData>() {
            @Override
            public void call(ReportPaymentData result) {
                Debug.log("ReportPaymentRequest success " + result.isSuccess());
                if (result.isSuccess()) {
                    ReportPaymentStatistics.sendSuccess();
                } else {
                    ReportPaymentStatistics.sendFail();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Debug.error("ReportPaymentRequest error " + throwable);
                ReportPaymentStatistics.sendFail();
            }
        });
    }
}
