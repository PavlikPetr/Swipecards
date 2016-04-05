package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.utils.social.OkAuthorizer;

/**
 * Created by ppavlik on 05.04.16.
 * methods for purchases state
 */
public class PurchasesEvents {

    public static void purchaseSuccess(int productsCount, String productType, String productId, String currencyCode, double price, String transactionId) {
        new ReportPayment(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()), transactionId, price, currencyCode).exec();
    }
}
