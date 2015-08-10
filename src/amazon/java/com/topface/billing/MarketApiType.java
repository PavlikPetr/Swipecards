package com.topface.billing;

/**
 * Типы платежей, разные для разных платформ.
 * Так же в зависимости от типа платежей определяется и тип клиента, отправляемый на сервер
 */
public enum MarketApiType {
    GOOGLE_PLAY("google-play-v2"),
    AMAZON("amazon"),
    NOKIA_STORE("android-nokia");


    private final String mClientType;

    MarketApiType(String clientType) {
        mClientType = clientType;
    }

    public String getClientType() {
        return mClientType;
    }
}
