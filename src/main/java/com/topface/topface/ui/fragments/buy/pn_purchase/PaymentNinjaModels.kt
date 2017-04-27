package com.topface.topface.ui.fragments.buy.pn_purchase

import android.os.Parcel
import android.os.Parcelable
import java.util.*

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
 * Модель продукта Payment Ninja
 *
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
 * @param currencyCode - строковый код валюты
 * @param subscriptionInfo - Инфо о подписке
 */
data class PaymentNinjaProduct(var id: String, var showType: Int, var titleTemplate: String, var totalPriceTemplate: String,
                               var isSubscription: Boolean, var period: Int, var price: Int, var type: String,
                               var value: Int, var trialPeriod: Int, var displayOnBuyScreen: Boolean,
                               var durationTitle: String, var divider: Float, var typeOfSubscription: Int,
                               val currencyCode: String, var subscriptionInfo: PaymentNinjaSubscriptionInfo) : Parcelable {

    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readByte().toInt() == 1,
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readByte().toInt() == 1,
            source.readString(),
            source.readFloat(),
            source.readInt(),
            source.readString(),
            source.readParcelable(PaymentNinjaSubscriptionInfo::class.java.classLoader)
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) =
            dest?.let {
                it.writeString(id)
                it.writeInt(showType)
                it.writeString(titleTemplate)
                it.writeString(totalPriceTemplate)
                it.writeByte((if (isSubscription) 1 else 0).toByte())
                it.writeInt(period)
                it.writeInt(price)
                it.writeString(type)
                it.writeInt(value)
                it.writeInt(trialPeriod)
                it.writeByte((if (displayOnBuyScreen) 1 else 0).toByte())
                it.writeString(durationTitle)
                it.writeFloat(divider)
                it.writeInt(typeOfSubscription)
                it.writeString(currencyCode)
                it.writeParcelable(subscriptionInfo, flags)
            } ?: Unit

    override fun describeContents() = 0

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PaymentNinjaProduct> = object : Parcelable.Creator<PaymentNinjaProduct> {
            override fun createFromParcel(source: Parcel): PaymentNinjaProduct = PaymentNinjaProduct(source)
            override fun newArray(size: Int): Array<PaymentNinjaProduct?> = arrayOfNulls(size)
        }
    }
}

/**
 * Модель с описанием функционала автопополнения
 *
 * @param text - Текст с опяснением услуги автопополнения
 * @param url - Ссылка на документацию о правилах по оказанию услуги
 */
data class PaymentNinjaSubscriptionInfo(var text: String, var url: String) : Parcelable {

    constructor(source: Parcel) : this(source.readString(), source.readString())

    override fun writeToParcel(dest: Parcel?, flags: Int) =
            dest?.let {
                it.writeString(text)
                it.writeString(url)
            } ?: Unit

    override fun describeContents() = 0

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PaymentNinjaSubscriptionInfo> = object : Parcelable.Creator<PaymentNinjaSubscriptionInfo> {
            override fun createFromParcel(source: Parcel): PaymentNinjaSubscriptionInfo = PaymentNinjaSubscriptionInfo(source)
            override fun newArray(size: Int): Array<PaymentNinjaSubscriptionInfo?> = arrayOfNulls(size)
        }
    }
}

/**
 * Полный список продуктов Payment Ninja
 *
 * @param products - Список продуктов
 */
data class PaymentNinjaProductsList(var products: Array<PaymentNinjaProduct>) {
    override fun equals(other: Any?) =
            other?.let { (it as? PaymentNinjaProductsList)?.let { Arrays.equals(it.products, products) } ?: false } ?: false

    override fun hashCode() = products.hashCode()
}