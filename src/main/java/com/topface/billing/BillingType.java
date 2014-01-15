package com.topface.billing;

/**
 * Типы платежей, разные для разных платформ.
 * Так же в зависимости от типа платежей определяется и тип клиента, отправляемый на сервер
 */
public enum BillingType {
    GOOGLE_PLAY("google-play-v2"),
    AMAZON("amazon");

    private final String mClientType;

    BillingType(String clientType) {
        mClientType = clientType;
    }

    public String getClientType() {
        return mClientType;
    }
}
