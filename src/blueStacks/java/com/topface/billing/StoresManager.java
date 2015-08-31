package com.topface.billing;

import android.content.Context;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.GooglePlay;

/**
 * Created by ppetr on 28.08.15.
 * add stores for current flavour
 */
public class StoresManager {

    public static void addStores(Context context, OpenIabHelper.Options.Builder optsBuilder) {
        optsBuilder.addAvailableStores(new GooglePlay(context, null));
        optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_GOOGLE);
    }
}
