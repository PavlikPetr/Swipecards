package com.topface.billing;

import android.content.Context;

import com.topface.topface.BuildConfig;
import com.topface.topface.requests.ApiRequest;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.AmazonAppstore;

/**
 * Created by ppetr on 28.08.15.
 * add stores for current flavour
 */
public class StoresManager {

    public static void addStores(Context context, OpenIabHelper.Options.Builder optsBuilder) {
        //Нужно для тестирования покупок в Amazon
        if (BuildConfig.DEBUG) {
            optsBuilder.addAvailableStores(new AmazonAppstore(context) {
                public boolean isBillingAvailable(String packageName) {
                    return true;
                }
            });
        } else {
            optsBuilder.addAvailableStores(new AmazonAppstore(context));
        }
        optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_AMAZON);
    }

    public static ApiRequest getPaymentwallProductsRequest() {
        return null;
    }
}
