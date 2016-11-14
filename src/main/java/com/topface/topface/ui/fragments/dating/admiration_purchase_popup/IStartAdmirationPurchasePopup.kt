package com.topface.topface.ui.fragments.dating.admiration_purchase_popup

import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.View

/**
 * Показать попап посхищений
 * Created by siberia87 on 01.11.16.
 */
interface IStartAdmirationPurchasePopup {
    fun startAnimateAdmirationPurchasePopup(transitionView: View, @ColorInt fabColorResId: Int,
                                            @DrawableRes fabIconResId: Int)
}