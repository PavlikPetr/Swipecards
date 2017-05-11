package com.topface.topface.ui.settings.payment_ninja.components

import com.topface.topface.R
import com.topface.topface.databinding.PaymentNinjaPurchaseItemWithSubtitleBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.SubscriptionInfo
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetData
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CANCEL_SUBSCRIPTION_BOTTOM_SHEET
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.ModalBottomSheetType.Companion.CANCEL_AUTOFILLING_BOTTOM_SHEET
import com.topface.topface.ui.settings.payment_ninja.view_models.PaymentNinjaPurchasesItemWithSubtitle

/**
 * Компонент для отображения информации о подписке
 * Created by petrp on 09.03.2017.
 */
class SubscriptionComponent(private val mNavigator: FeedNavigator) : AdapterComponent<PaymentNinjaPurchaseItemWithSubtitleBinding, SubscriptionInfo>() {

    override val itemLayout: Int
        get() = R.layout.payment_ninja_purchase_item_with_subtitle
    override val bindingClass: Class<PaymentNinjaPurchaseItemWithSubtitleBinding>
        get() = PaymentNinjaPurchaseItemWithSubtitleBinding::class.java

    override fun bind(binding: PaymentNinjaPurchaseItemWithSubtitleBinding, data: SubscriptionInfo?, position: Int) {
        data?.let {
            binding.viewModel = PaymentNinjaPurchasesItemWithSubtitle(mSubscription = it, onLongClickListener = {
                if (it.enabled) {
                    mNavigator.showPaymentNinjaBottomSheet(ModalBottomSheetData(ModalBottomSheetType(
                            if (it.type == SubscriptionInfo.SUBSCRIPTION_TYPE_PREMIUM)
                                CANCEL_SUBSCRIPTION_BOTTOM_SHEET
                            else
                                CANCEL_AUTOFILLING_BOTTOM_SHEET
                    ), it))
                    true
                } else false
            })
        }
    }
}