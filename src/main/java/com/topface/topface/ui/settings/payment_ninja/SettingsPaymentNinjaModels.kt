package com.topface.topface.ui.settings.payment_ninja

import android.os.Parcel
import android.os.Parcelable
import com.topface.topface.utils.Utils

/**
 * Модельки для экрана платежей
 * Created by ppavlik on 06.03.17.
 */

/**
 * Настройки для Payment Ninja
 *
 * @param enable - включены ли платежи через payment ninja
 * @param lastDigit - последние 4 цифры из номера карты
 * @param type - тип карты (Maestro, Discover, American Express, Visa Electron, Diners Club, Laser, JCB, МИР)
 * @param email - email пользователя TF
 * @param publicKey - публичный ключ для совершения платежей через Payment Ninja
 */
data class PaymentInfo(var enable: Boolean = false, var lastDigit: String = Utils.EMPTY, var type: String = Utils.EMPTY,
                       var email: String = Utils.EMPTY, var publicKey: String = Utils.EMPTY) : Parcelable {

    constructor(source: Parcel) : this(
            source.readByte().toInt() == 1,
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString()
    )

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PaymentInfo> = object : Parcelable.Creator<PaymentInfo> {
            override fun createFromParcel(source: Parcel): PaymentInfo = PaymentInfo(source)
            override fun newArray(size: Int): Array<PaymentInfo?> = arrayOfNulls(size)
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) =
            dest?.let {
                it.writeByte((if (enable) 1 else 0).toByte())
                it.writeString(lastDigit)
                it.writeString(type)
                it.writeString(email)
                it.writeString(publicKey)
            } ?: Unit
}

/**
 * Данные карты для Payment Ninja
 *
 * @param lastDigit - последние 4 цифры из номера карты
 * @param type - тип карты (Maestro, Discover, American Express, Visa Electron, Diners Club, Laser, JCB, МИР)
 */
data class CardInfo(var lastDigit: String = Utils.EMPTY, var type: String = Utils.EMPTY) : Parcelable {

    constructor(source: Parcel) : this(source.readString(), source.readString())

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CardInfo> = object : Parcelable.Creator<CardInfo> {
            override fun createFromParcel(source: Parcel): CardInfo = CardInfo(source)
            override fun newArray(size: Int): Array<CardInfo?> = arrayOfNulls(size)
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) =
            dest?.let {
                it.writeString(lastDigit)
                it.writeString(type)
            } ?: Unit
}

/**
 * Список карт пользователя
 *
 * @param items - массив карт
 */
data class CardList(var items: Array<CardInfo>) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CardList> = object : Parcelable.Creator<CardList> {
            override fun createFromParcel(source: Parcel): CardList = CardList(source)
            override fun newArray(size: Int): Array<CardList?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readParcelableArray(CardInfo::class.java.classLoader) as Array<CardInfo>)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelableArray(items, 0)
    }
}

/**
 * Данные о подписке, для отображения на экране "Платежи"
 *
 * @param id - id подписки
 * @param type - тип подписки. 0 - подписка VIP, 1 - подписка монеты, 2 - подписка на симпатии, 3- автопополнение
 * @param title - название продукта
 * @param expire - дата окончания действия
 * @param enabled - информация об активности подписки/автопоплнения (false - отменено)
 */
data class SubscriptionInfo(var id: String, var type: Int, var title: String, var expire: Long, var enabled: Boolean)

/**
 * Список подписок пользователя
 * @param userSubscriptions - список подписок/автопоплнений пользователя
 */
data class UserSubscriptions(var userSubscriptions: Array<SubscriptionInfo>)

/**
 * Итем "Поддержка" для списка на экране "Платежи"
 */
data class PaymentNinjaHelp(private val mDiffTemp: Int = 0)

/**
 * Лоадер на время запроса
 */
data class PaymnetNinjaPurchasesLoader(private val mDiffTemp: Int = 0)