package com.topface.topface.banners.ad_providers;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;

import ru.adcamp.ads.AdsManager;

/**
 * Created by kirussell on 12/01/15.
 * Utils methods for ads' sdks credentials
 */
public class CredentialsUtils {

    private static boolean mAdcampInitialized = false;

    public static void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            Options options = CacheProfile.getOptions();
            if (options != null && options.containsBannerType(AdProvidersFactory.BANNER_ADCAMP)) {
                Context context = App.getContext();
                AdsManager.getInstance().initialize(
                        context,
                        context.getString(R.string.adcamp_app_id),
                        context.getString(R.string.adcamp_app_secret),
                        context.getResources().getBoolean(R.bool.adcamp_logging_enabled),
                        Log.VERBOSE
                );
                mAdcampInitialized = true;
            }
        }
    }

    static boolean isAdcampInitialized() {
        return mAdcampInitialized;
    }
}