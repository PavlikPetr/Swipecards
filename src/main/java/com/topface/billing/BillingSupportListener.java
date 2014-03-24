package com.topface.billing;

/**
 * Интерфейс определения поддержки платеюей
 */
public interface BillingSupportListener {
    /**
     * Вызывается, если платежи внутри приложения доступны
     */
    public void onInAppBillingSupported();

    /**
     * Вызывается, если доступна подписка
     */
    public void onSubscriptionSupported();

    /**
     * Вызывается, если платежи не доступы
     */
    public void onInAppBillingUnsupported();

    /**
     * Вызывается, если платежи доступны
     */
    public void onSubscriptionUnsupported();
}
