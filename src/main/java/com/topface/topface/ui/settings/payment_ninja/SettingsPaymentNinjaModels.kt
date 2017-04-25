package com.topface.topface.ui.settings.payment_ninja

import android.os.Parcel
import android.os.Parcelable
import com.topface.billing.ninja.AddCardRequest
import com.topface.topface.utils.Utils

/**
 * Модельки для экрана платежей
 * Created by ppavlik on 06.03.17.
 */

/**
 * Настройки для Payment Ninja
 *
 * @param enabled - включены ли платежи через payment ninja
 * @param lastDigits - последние 4 цифры из номера карты
 * @param type - тип карты (Maestro, Discover, American Express, Visa Electron, Diners Club, Laser, JCB, МИР)
 * @param email - email пользователя TF
 * @param projectKey - публичный ключ для совершения платежей через Payment Ninja
 * @param apiUrl - линка для обращения к PN
 */
data class PaymentInfo(var enabled: Boolean = false, var lastDigits: String = Utils.EMPTY, var type: String = Utils.EMPTY,
                       var email: String = Utils.EMPTY, var projectKey: String = Utils.EMPTY, var apiUrl: String = AddCardRequest.ADD_CARD_LINK) : Parcelable {

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
                it.writeByte((if (enabled) 1 else 0).toByte())
                it.writeString(lastDigits)
                it.writeString(type)
                it.writeString(email)
                it.writeString(projectKey)
            } ?: Unit
}

/**
 * Данные карты для Payment Ninja
 *
 * @param lastFour - последние 4 цифры из номера карты
 * @param type - тип карты (Maestro, Discover, American Express, Visa Electron, Diners Club, Laser, JCB, МИР)
 * @param hash - Хэш карты внутри TopFace
 * @param expire - дата окончания действия карты
 */
data class CardInfo(var lastFour: String = Utils.EMPTY, var type: String = Utils.EMPTY, var hash: String = Utils.EMPTY,
                    var expire: Long = 0) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CardInfo> = object : Parcelable.Creator<CardInfo> {
            override fun createFromParcel(source: Parcel): CardInfo = CardInfo(source)
            override fun newArray(size: Int): Array<CardInfo?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(), source.readLong())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(lastFour)
        dest?.writeString(type)
        dest?.writeString(hash)
        dest?.writeLong(expire)
    }
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
 * @param type - тип подписки. {premium, coinsAutoRefill}
 * @param title - название продукта
 * @param expire - дата окончания действия
 * @param enabled - информация об активности подписки/автопоплнения (false - отменено)
 */
data class SubscriptionInfo(var type: String, var title: String, var expire: Long, var enabled: Boolean) : Parcelable {
    companion object {
        const val SUBSCRIPTION_TYPE_PREMIUM = "premium"
        const val SUBSCRIPTION_TYPE_AUTO_REFILL = "coinsAutoRefill"
        @JvmField val CREATOR: Parcelable.Creator<SubscriptionInfo> = object : Parcelable.Creator<SubscriptionInfo> {
            override fun createFromParcel(source: Parcel): SubscriptionInfo = SubscriptionInfo(source)
            override fun newArray(size: Int): Array<SubscriptionInfo?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readLong(), 1 == source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(type)
        dest?.writeString(title)
        dest?.writeLong(expire)
        dest?.writeInt((if (enabled) 1 else 0))
    }
}

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