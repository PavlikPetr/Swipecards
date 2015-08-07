package com.topface.topface.utils;

import com.topface.topface.App;
import android.app.Activity;

import com.topface.topface.BuildConfig;

public class MarketApiManager {
    BaseMarketApiManager mServicesManager;

    public MarketApiManager() {
        mServicesManager = MarketApiManagerUtils.getMarketManagerByType();
    }

    public void onResume() {
        mServicesManager.onResume();
    }


    public int getResultCode() {
        return mServicesManager.getResultCode();
    }

    public void onProblemResolve(Activity activity) {
        mServicesManager.onProblemResolve(activity);
    }

    public int getButtonTextId() {
        return mServicesManager.getButtonTextId();
    }

    public boolean isButtonVisible() {
        return mServicesManager.isButtonVisible();
    }

    public boolean isTitleVisible() {
        return mServicesManager.isTitleVisible();
    }

    public int getTitleTextId() {
        return mServicesManager.getTitleTextId();
    }

    public boolean isMarketApiSupportByUs() {
        return mServicesManager.isMarketApiSupportByUs();
    }

    public boolean isMarketApiAvailable() {
        return mServicesManager.isMarketApiAvailable();
    }
}
