package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

/**
 * Интерфейс отображения bottom sheet для раздела "Платежи"
 * Created by ppavlik on 13.03.17.
 */

interface ISettingsPaymentNinjaBottomSheetInterface {
    /**
     * Показать bottomSheet для работы с картой пользователя
     */
    fun showCardBottomSheet()

    /**
     * Показать bottomSheet для с подпиской пользователя
     * @param isSubscriptionActive - информация о том активна ли подписка/автопополнение (false - отменено)
     */
    fun showSubscriptionBottomSheet(isSubscriptionActive: Boolean)
}