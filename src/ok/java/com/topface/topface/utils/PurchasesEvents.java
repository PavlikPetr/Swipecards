package com.topface.topface.utils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.social.OkAuthorizer;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by ppavlik on 05.04.16.
 * methods for purchases state
 */
public class PurchasesEvents {

    public static void purchaseSuccess(int productsCount, String productType, String productId, String currencyCode, double price, String transactionId) {
        Debug.log("PurchasesEvents prepare to send ReportPayment to OK");
        new ReportPaymentRequest(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()), transactionId, price, currencyCode).getObservable().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
    }
}
