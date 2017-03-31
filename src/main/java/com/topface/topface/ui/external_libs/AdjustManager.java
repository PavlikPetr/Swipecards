package com.topface.topface.ui.external_libs;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.kochava.android.tracker.Feature;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.ActivityLifreCycleData;
import com.topface.topface.data.Products;
import com.topface.topface.requests.ReferrerLogRequest;
import com.topface.topface.state.LifeCycleState;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.external_libs.adjust.AdjustAttributeData;
import com.topface.topface.utils.FlurryManager;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by ppetr on 30.03.16.
 * Adjust integration
 */
public class AdjustManager {

    private static final String ADJUST_TOKEN = "sewqil33vev4";
    private static final String REGISTRATION_TOKEN = "h0g3lj";
    private static final String PURCHASE_TOKEN = "trfawd";
    private static final String FIRST_PAY_TOKEN = "h75za2";

    private TopfaceAppState mAppState;
    private LifeCycleState mLifeCycleState;
    private boolean mIsInitialized;

    public AdjustManager() {
        mAppState = App.getAppComponent().appState();
        mLifeCycleState = App.getAppComponent().lifeCycleState();
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
                FlurryManager.getInstance().sendReferrerEvent(data);
                new ReferrerLogRequest(App.getContext(), null, null, data).exec();
            }
        });
        config.setLogLevel(Debug.isDebugLogsEnabled() ? LogLevel.VERBOSE : LogLevel.ASSERT);
        Adjust.onCreate(config);
        mLifeCycleState.getObservable(ActivityLifreCycleData.class).filter(new Func1<ActivityLifreCycleData, Boolean>() {
            @Override
            public Boolean call(ActivityLifreCycleData activityLifreCycleData) {
                return activityLifreCycleData != null
                        && (activityLifreCycleData.getState() == ActivityLifreCycleData.RESUMED
                        || activityLifreCycleData.getState() == ActivityLifreCycleData.PAUSED);
            }
        })
                .subscribe(new Action1<ActivityLifreCycleData>() {
                    @Override
                    public void call(ActivityLifreCycleData activityLifreCycleData) {
                        if (activityLifreCycleData.getState() == ActivityLifreCycleData.RESUMED) {
                            Adjust.onResume();
                        } else if (activityLifreCycleData.getState() == ActivityLifreCycleData.PAUSED) {
                            Adjust.onPause();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
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
