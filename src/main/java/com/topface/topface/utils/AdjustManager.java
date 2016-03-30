package com.topface.topface.utils;

import android.content.res.Resources;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.ui.BaseFragmentActivity;

import rx.functions.Action1;

/**
 * Created by ppetr on 30.03.16.
 * Adjust integration
 */
public class AdjustManager {

    private static final String ADJUST_TOKEN = "sewqil33vev4";

    public static void initAdjust() {
        AdjustConfig config = new AdjustConfig(App.getContext(), ADJUST_TOKEN, Debug.isDebugLogsEnabled() ?
                AdjustConfig.ENVIRONMENT_SANDBOX :
                AdjustConfig.ENVIRONMENT_PRODUCTION);
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
            }
        });
        config.setLogLevel(Debug.isDebugLogsEnabled() ? LogLevel.VERBOSE : LogLevel.ASSERT);
        Adjust.onCreate(config);
        BaseFragmentActivity.getLifeCycleObservable().subscribe(new Action1<BaseFragmentActivity.ActivityLifecycle>() {
            @Override
            public void call(BaseFragmentActivity.ActivityLifecycle lifecycle) {
                switch (lifecycle) {
                    case RESUMED:
                        Adjust.onResume();
                        break;
                    case PAUSED:
                        Adjust.onPause();
                        break;
                }
            }
        });
    }

    public static void sendRevenue(String eventName, double revenue) {
        AdjustEvent event = new AdjustEvent(eventName);
        event.setRevenue(revenue, Products.USD);
        Adjust.trackEvent(event);
    }

    public static void sendRegistration(String socialName) {
        Resources res = App.getContext().getResources();
        AdjustEvent event = new AdjustEvent(res.getString(R.string.appsflyer_registration));
        event.addPartnerParameter(res.getString(R.string.adjust_registration_social), socialName);
    }
}
