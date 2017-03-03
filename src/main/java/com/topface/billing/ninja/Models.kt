package com.topface.billing.ninja

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