package com.topface.billing;

import android.content.Context;

import com.topface.topface.requests.ApiRequest;

import org.onepf.oms.OpenIabHelper;

/**
 * Created by ppetr on 28.08.15.
 * add stores for current flavour
 */
public class StoresManager {

    public static void addStores(Context context, OpenIabHelper.Options.Builder optsBuilder) {
        // we don't use google billing for ifree
    }

    public static ApiRequest getPaymentwallProductsRequest() {
        return null;
    }
}
