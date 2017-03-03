package com.topface.topface.ui.fragments.buy.pn_purchase

/**
 * Модельки для экрана покупок продуктов Payment Ninja
 * Created by ppavlik on 03.03.17.
 */
// Title of Payment Ninja products
class BuyScreenTitle

// Title for a Likes products section on Payment Ninja purchase screen
class BuyScreenLikesSection

// Title for a Coins products section on Payment Ninja purchase screen
class BuyScreenCoinsSection

// Show stub if Payment Ninja products is unavailable
class BuyScreenProductUnavailable

/**
 * @param id - id мобильного продукта
 * @param showType -Тип отображения 0 - обычная кнопка покупки 1 - выделенная кнопка покупки 2 - неактивная кнопка покупки
 * @param titleTemplate - Шаблон текста на кнопке
 * @param totalPriceTemplate - Шаблон текста для итоговой цены
 * @param isSubscription - Является ли продукт подпиской
 * @param period - Период подписки в днях (для подписок)
 * @param price - Цена
 * @param type - Тип продуктапокупка монет - coins покупка энергии - energy покупка лидерства - leader покупка премиумов - premium покупка лайков - likes покупка монет по подписке - coinsSubscription
 * @param value - Поле с дополнительной информацией по продуктуДля подписок - время подписки (в месяцах), для монет количество монет, для лайков - 1
 * @param trialPeriod - Триальный период подписки в днях
 * @param displayOnBuyScreen - Отображать продукт на экране покупок или нет
 * @param durationTitle - Заголовок обозначающий время продукта
 * @param divider - Возвращает делитель для подсчета суммы, к примеру неделя- 7/30 месяца = 0.23
 * @param typeOfSubscription - 0- не подписка 1- автопополнение (когда кончится) 2- подписка (раз в месяц)
 * @param infoOfSubscription - Инфо о подписке
 */
data class PaymentNinjaProduct(var id: String, var showType: Int, var titleTemplate: String, var totalPriceTemplate: String,
                               var isSubscription: Boolean, var period: Int, var price: Int, var type: String,
                               var value: Int, var trialPeriod: Int, var displayOnBuyScreen: Boolean,
                               var durationTitle: String, var divider: Float, var typeOfSubscription: Int,
                               var infoOfSubscription: PaymentNinjaSubscriptionInfo)

/**
 * @param text - Текст с опяснением услуги автопополнения
 * @param url - Ссылка на документацию о правилах по оказанию услуги
 */
data class PaymentNinjaSubscriptionInfo(var text: String, var url: String)

/**
 * @param products - Список продуктов
 */
data class PaymentNinjaProductsList(var products: Array<PaymentNinjaProduct>)