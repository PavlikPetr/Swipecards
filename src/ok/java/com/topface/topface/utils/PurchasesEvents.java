package com.topface.topface.utils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.social.OkAuthorizer;

/**
 * Created by ppavlik on 05.04.16.
 * methods for purchases state
 */
public class PurchasesEvents {

    public static void purchaseSuccess(int productsCount, String productType, String productId, String currencyCode, double price, String transactionId) {
        Debug.log("PurchasesEvents prepare to send ReportPayment to OK");
        new ReportPayment(new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()), transactionId, price, currencyCode).exec();
    }
}
