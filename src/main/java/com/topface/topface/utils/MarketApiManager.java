package com.topface.topface.utils;

import android.content.Context;
import android.view.View;

import com.topface.topface.BuildConfig;

public class MarketApiManager extends BaseMarketApiManager {
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

    @Override
    public void onResume() {
        mServicesManager.onResume();
    }

    @Override
    public View getView() {
        return mServicesManager.getView();
    }

    @Override
    public String getMessage() {
        return mServicesManager.getMessage();
    }

    @Override
    public int getResultCode() {
        return mServicesManager.getResultCode();
    }

    @Override
    public boolean isServicesAvailable() {
        return mServicesManager.isServicesAvailable();
    }
}
