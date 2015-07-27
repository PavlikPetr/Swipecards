package com.topface.topface.utils;

import com.topface.topface.BuildConfig;

public class MarketApiManagerUtils {

    public static BaseMarketApiManager getMarketManagerByType(){
        switch (BuildConfig.MARKET_API_TYPE) {
            case GOOGLE_PLAY:
                return new GoogleMarketApiManager();
            case AMAZON:
                return new AmazonMarketApiManager();
            case NOKIA_STORE:
                return new NokiaMarketApiManager();
            case I_FREE:
                return new IFreeMarketApiManager();
        }
        return null;
    }
}