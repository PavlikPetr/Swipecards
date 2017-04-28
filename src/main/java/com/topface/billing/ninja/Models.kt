package com.topface.billing.ninja

import android.os.Parcel
import android.os.Parcelable
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.utils.Utils

/**
 * Models used for Payment Ninja
 * Created by m.bayutin on 02.03.17.
 */

/**
 * lastFour - {String} Last four digits of a card number.
 * mask - {String} Card number mask. Only last four digits are visible, others are hidden with asterisks.
 * type - {String} Card type (e.g. visa, mastercard).
 * expirationMonth - {Integer} Card expiration month.
 * expirationYear - {Integer} Card expiration year.
 */
data class CardModel(val lastFour: String, val mask: String, val type: String, val expirationMonth: Int, val expirationYear: Int)

/**
 * id - {String} Identifier of a token.
 * expiresAt - {String} date in ISO 8601 format, Date when token expires.
 * card - {CardModel} object, Card object
 */
data class AddCardResponse(val id: String, val expiresAt: String, val card: CardModel)

/**
 * project - {String} Public key of your project.
 * number - {String} Card number without any separators.
 * expiration_month - {String} Integer representing the card’s expiration month.
 * expiration_year - {String} Four digit number representing card’s expiration year.
 * security_code - {String} Card security code (CVC, CVV2).
 */
data class AddCardModel(val project: String, val number: String, val expirationMonth: String, val expirationYear: String, val securityCode: String, val email: String)

/**
 * used in paymentNinja.addCard request
 * token - {String} Временный токен, который уже получил клиент
 * email - {String} email пользователя
 * */
data class SendCardTokenModel(val token: String, val email: String)

/**
 * Settings for 3D secure purchase
 *
 * @param errorCode - tf api error code
 * @param MD - put it to POST
 * @param PaReq - put it to POST
 * @param termUrl - put it to POST
 * @param acsUrl - url for 3d secure validation
 * @param paymentSuccessUrl - url in success of 3d secure validation
 * @param paymentFailUrl - url in faile of 3d secure validation
 */
data class ThreeDSecureParams(val errorCode: Int = 0, val MD: String = Utils.EMPTY, val PaReq: String = Utils.EMPTY,
                              val termUrl: String = Utils.EMPTY, val acsUrl: String = Utils.EMPTY,
                              val paymentSuccessUrl: String = Utils.EMPTY, val paymentFailUrl: String = Utils.EMPTY) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ThreeDSecureParams> = object : Parcelable.Creator<ThreeDSecureParams> {
            override fun createFromParcel(source: Parcel): ThreeDSecureParams = ThreeDSecureParams(source)
            override fun newArray(size: Int): Array<ThreeDSecureParams?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt(), source.readString(), source.readString(),
            source.readString(), source.readString(), source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(errorCode)
        dest?.writeString(MD)
        dest?.writeString(PaReq)
        dest?.writeString(termUrl)
        dest?.writeString(acsUrl)
        dest?.writeString(paymentSuccessUrl)
        dest?.writeString(paymentFailUrl)
    }
}

/**
 * Error object of PN purchase
 *
 * @param settings - Settings for 3D secure purchase
 * @param product - PN product
 */
data class PurchaseError(val settings: ThreeDSecureParams = ThreeDSecureParams(), val product: PaymentNinjaProduct = PaymentNinjaProduct()) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PurchaseError> = object : Parcelable.Creator<PurchaseError> {
            override fun createFromParcel(source: Parcel): PurchaseError = PurchaseError(source)
            override fun newArray(size: Int): Array<PurchaseError?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readParcelable<ThreeDSecureParams>(ThreeDSecureParams::class.java.classLoader),
            source.readParcelable<PaymentNinjaProduct>(PaymentNinjaProduct::class.java.classLoader))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(settings, 0)
        dest?.writeParcelable(product, 0)
    }
}