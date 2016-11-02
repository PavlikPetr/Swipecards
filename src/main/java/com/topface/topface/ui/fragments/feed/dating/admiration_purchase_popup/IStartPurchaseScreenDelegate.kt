package com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup

/**
 * Интерфейс для делегирования экранов покупки випа и монет
 * Created by siberia87 on 03.11.16.
 */
interface IStartPurchaseScreenDelegate {
    fun startCoinScreenPurchase()
    fun startVIPScreenPurchase()
//    fun <T, R> startCoinScreenPurchase(receiver: T, block: T.() -> R): R = receiver.block()
}