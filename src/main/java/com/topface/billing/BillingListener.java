package com.topface.billing;

public interface BillingListener {

    /**
     * Покупка завершилась удачно
     */
    public void onPurchased(String sku);

    /**
     * Произошла ошибка при покупке
     */
    public void onError();

    /**
     * Если пользователь сам отменил платеж
     */
    public void onCancel();

}
