package com.topface.topface.utils;

import android.content.Context;

import com.topface.topface.BuildConfig;

public class MarketApiManager {
    BaseMarketApiManager mServicesManager;

    public MarketApiManager() {
        switch (BuildConfig.MARKET_API_TYPE) {
            case GOOGLE_PLAY:
                mServicesManager = new GoogleMarketApiManager();
                break;
            case AMAZON:
                mServicesManager = new AmazonMarketApiManager();
                break;
            case NOKIA_STORE:
                mServicesManager = new NokiaMarketApiManager();
                break;
        }
    }

    public void onResume() {
        mServicesManager.onResume();
    }


    public int getResultCode() {
        return mServicesManager.getResultCode();
    }

    public void onProblemResolve(Context context) {
        mServicesManager.onProblemResolve(context);
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
