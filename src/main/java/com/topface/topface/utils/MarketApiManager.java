package com.topface.topface.utils;

import android.content.Context;
import android.view.View;

import com.topface.topface.BuildConfig;

public class MarketApiManager {
    BaseMarketApiManager mServicesManager;

    public MarketApiManager(Context context) {
        switch (BuildConfig.MARKET_API_TYPE) {
            case GOOGLE_PLAY:
                mServicesManager = new GoogleMarketApiManager(context);
                break;
            case AMAZON:
                mServicesManager = new AmazonMarketApiManager(context);
                break;
            case NOKIA_STORE:
                mServicesManager = new NokiaMarketApiManager(context);
                break;
        }
    }

    public void onResume() {
        mServicesManager.onResume();
    }

    public View getView() {
        return mServicesManager.getView();
    }

    public int getResultCode() {
        return mServicesManager.getResultCode();
    }

    public void onButtonClick() {
        mServicesManager.onButtonClick();
    }

    public String getButtonText() {
        return mServicesManager.getButtonText();
    }

    public boolean isButtonVisible() {
        return mServicesManager.isButtonVisible();
    }

    public String getMessage() {
        return mServicesManager.getMessage();
    }

    public boolean isServicesAvailable() {
        return mServicesManager.isServicesAvailable();
    }
}
