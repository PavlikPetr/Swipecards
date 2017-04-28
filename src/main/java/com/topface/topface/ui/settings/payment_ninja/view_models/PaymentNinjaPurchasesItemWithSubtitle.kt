package com.topface.topface.ui.settings.payment_ninja.view_models

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.settings.payment_ninja.SubscriptionInfo
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getString
import java.text.SimpleDateFormat
import java.util.*

/**
 * Вью-модель для работы с элементом списка, который имеет только title
 * Created by petrp on 12.03.2017.
 */
class PaymentNinjaPurchasesItemWithSubtitle(private val mSubscription: SubscriptionInfo,
                                            val onClickListener: () -> Unit = {},
                                            val onLongClickListener: () -> Boolean = { false }) {
    val title = ObservableField(mSubscription.title)
    val subTitle = ObservableField(getSubtitleText())
    val subTitleColor = ObservableInt(getSubtitleTextColor())
    val icon = ObservableInt(if (mSubscription.type == SubscriptionInfo.SUBSCRIPTION_TYPE_PREMIUM) R.drawable.ic_crown_left_menu else R.drawable.ic_coins_small)

    private fun getSubtitleText() =
            if (mSubscription.type == SubscriptionInfo.SUBSCRIPTION_TYPE_PREMIUM) {
                String.format(App.getCurrentLocale(),
                        if (mSubscription.enabled) R.string.ninja_subscription_expiration.getString() else R.string.ninja_subscription_cancelled.getString(),
                        SimpleDateFormat("d MMMM", Locale(App.getLocaleConfig().applicationLocale)).format(mSubscription.expire).toLowerCase())
            } else {
                (if (mSubscription.enabled) R.string.ninja_autofilling_activated else R.string.ninja_autofilling_cancelled).getString()
            }

    private fun getSubtitleTextColor() =
            (if (mSubscription.enabled) R.color.ninja_payments_screen_item_subtitle_color else R.color.ninja_payments_screen_item_subtitle_accent_color).getColor()
}
