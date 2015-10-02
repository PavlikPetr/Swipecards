package com.topface.topface.utils;

import com.topface.topface.BuildConfig;

public class MarketApiManagerUtils {

    public static BaseMarketApiManager getMarketManagerByType() {
        switch (BuildConfig.MARKET_API_TYPE) {
            case AMAZON:
                return new AmazonMarketApiManager();
            case NOKIA_STORE:
                return new NokiaMarketApiManager();
            case DERIVED:
                return new DerivedMarketApiManager();
        }
        return null;
    }
}