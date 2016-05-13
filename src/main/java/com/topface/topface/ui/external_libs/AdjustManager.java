package com.topface.topface.ui.external_libs;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.ActivityLifreCycleData;
import com.topface.topface.data.Products;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;
import com.topface.topface.utils.FlurryManager;

import javax.inject.Inject;

import rx.functions.Action1;

/**
 * Created by ppetr on 30.03.16.
 * Adjust integration
 */
public class AdjustManager {

    private static final String ADJUST_TOKEN = "sewqil33vev4";
    private static final String REGISTRATION_TOKEN = "h0g3lj";
    private static final String PURCHASE_TOKEN = "trfawd";
    private static final String FIRST_PAY_TOKEN = "h75za2";

    @Inject
    TopfaceAppState mAppState;
    private boolean mIsInitialized;

    public AdjustManager() {
        App.from(App.getContext()).inject(this);
    }

    public void initAdjust() {
        AdjustConfig config = new AdjustConfig(App.getContext(), ADJUST_TOKEN, Debug.isDebugLogsEnabled() ?
                AdjustConfig.ENVIRONMENT_SANDBOX :
                AdjustConfig.ENVIRONMENT_PRODUCTION);
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                AdjustAttributeData data = new AdjustAttributeData(attribution);
                mAppState.setData(data);
                Debug.log("AdjustManager", "onAttributionChanged attribution:" + JsonUtils.toJson(data));
                FlurryManager.getInstance().sendReferrerEvent(data);
            }
        });
        config.setLogLevel(Debug.isDebugLogsEnabled() ? LogLevel.VERBOSE : LogLevel.ASSERT);
        Adjust.onCreate(config);
        TrackedLifeCycleActivity.getLifeCycleObservable().subscribe(new Action1<ActivityLifreCycleData>() {
            @Override
            public void call(ActivityLifreCycleData lifecycleData) {
                switch (lifecycleData.state) {
                    case RESUMED:
                        Adjust.onResume();
                        break;
                    case PAUSED:
                        Adjust.onPause();
                        break;
                }
            }
        });
        mIsInitialized = true;
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public void sendPurchaseEvent(double revenue) {
        sendRevenue(PURCHASE_TOKEN, revenue);
    }

    public void sendFirstPayEvent(double revenue) {
        sendRevenue(FIRST_PAY_TOKEN, revenue);
    }

    private void sendRevenue(String eventToken, double revenue) {
        if (!checkSdkState()) {
            return;
        }
        AdjustEvent event = new AdjustEvent(eventToken);
        event.setRevenue(revenue, Products.USD);
        Adjust.trackEvent(event);
    }

    public void sendRegistrationEvent(String socialName) {
        if (!checkSdkState()) {
            return;
        }
        AdjustEvent event = new AdjustEvent(REGISTRATION_TOKEN);
        // временно убираю отправку данных о типе соц. сети в которой был авторизован пользователь
        //event.addCallbackParameter(App.getContext().getResources().getString(R.string.adjust_registration_social), socialName);
        Adjust.trackEvent(event);
    }

    private boolean checkSdkState() {
        if (!isInitialized()) {
            Debug.error("Call initAdjust() at first");
            return false;
        }
        return true;
    }
}
