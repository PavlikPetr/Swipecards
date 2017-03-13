package com.topface.topface.ui.settings.payment_ninja.view_models

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.SubscriptionInfo
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getCardName
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getString
import kotlin.properties.Delegates

/**
 * Вью-модель для работы с элементом списка, который имеет только title
 * Created by petrp on 12.03.2017.
 */

class PaymentNinjaPurchasesItemWithSubtitle(private val mSubscription: SubscriptionInfo, val onClickListener: () -> Unit) {
    val title = ObservableField(mSubscription.title)
    val subTitle = ObservableField(getSubtitleText())
    val subTitleColor = ObservableInt(getSubtitleTextColor())
    val icon = ObservableInt(getIcon())

    private fun getSubtitleText() =
            if (mSubscription.type == 0) {
                if (mSubscription.enabled) {
                    String.format(App.getCurrentLocale(),
                            R.string.ninja_subscription_expiration.getString(), getExpirationDate())
                } else {
                    R.string.ninja_subscription_cancelled.getString()
                }
            } else {
                (if (mSubscription.enabled) R.string.ninja_autofilling_activated else R.string.ninja_autofilling_cancelled).getString()
            }

    private fun getExpirationDate() = ""

    private fun getSubtitleTextColor() =
            (if (mSubscription.enabled) R.color.ninja_payments_screen_item_subtitle_color else R.color.ninja_payments_screen_item_subtitle_accent_color).getColor()

    private fun getIcon() =
            when (mSubscription.type) {
                0 -> R.drawable.ic_crown_left_menu
                1 -> R.drawable.ic_crown_left_menu
                2 -> R.drawable.ic_crown_left_menu
                3 -> R.drawable.ic_coins_small
                else -> R.drawable.ic_crown_left_menu
            }
}
