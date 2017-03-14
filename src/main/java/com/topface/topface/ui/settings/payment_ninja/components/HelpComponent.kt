package com.topface.topface.ui.settings.payment_ninja.components

import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.BottomSheetTitleBinding
import com.topface.topface.databinding.PaymentNinjaPurchaseItemTitleOnlyBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.settings.payment_ninja.CardInfo
import com.topface.topface.ui.settings.payment_ninja.PaymentNinjaHelp
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.BottomSheetTitle
import com.topface.topface.ui.settings.payment_ninja.bottom_sheet.view_models.SettingsPaymentNinjaBottomSheetItemViewModel
import com.topface.topface.ui.settings.payment_ninja.view_models.CardViewModel
import com.topface.topface.ui.settings.payment_ninja.view_models.PaymentNinjaPurchasesItemTitleOnlyViewModel
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Компонент для пункта поддержки
 * Created by petrp on 09.03.2017.
 */
class HelpComponent(private val mNavigator: FeedNavigator) : AdapterComponent<PaymentNinjaPurchaseItemTitleOnlyBinding, PaymentNinjaHelp>() {

    override val itemLayout: Int
        get() = R.layout.payment_ninja_purchase_item_title_only
    override val bindingClass: Class<PaymentNinjaPurchaseItemTitleOnlyBinding>
        get() = PaymentNinjaPurchaseItemTitleOnlyBinding::class.java

    override fun bind(binding: PaymentNinjaPurchaseItemTitleOnlyBinding, data: PaymentNinjaHelp?, position: Int) {
        data?.let {
            binding.viewModel = PaymentNinjaPurchasesItemTitleOnlyViewModel(onClickListener = {
                //todo дернуть показ экрана отправки сообщения в сапорт
            }).apply {
                title.set(R.string.ninja_support.getString())
                icon.set(R.drawable.ic_question_small)
            }
        }
    }

}