package com.topface.topface.ui.fragments.buy.pn_purchase

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.R
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString

/**
 * Вью модель полностью конфигурируемого текста
 * Created by ppavlik on 03.03.17.
 */
class BuyScreenTextViewModel(mText: String = "", val textColor: Int = R.color.black.getColor(),
                             val textSize: Int = R.dimen.dating_parent_item_title_size.getDimen().toInt(),
                             mTextVisibility: Int = View.VISIBLE,
                             val marginLeft: Float = 0f, val marginTop: Float = 0f, val marginRight: Float = 0f,
                             val marginBottom: Float = 0f, val paddingLeft: Float = 0f, val paddingTop: Float = 0f,
                             val paddingRight: Float = 0f, val paddingBottom: Float = 0f,
                             val background: Int = R.color.transparent,
                             val isAllCaps: Boolean = false,
                             val onClickListener: () -> Unit = {}) {
    val textVisibility = ObservableInt(mTextVisibility)
    val text = ObservableField(mText)

    companion object {
        // Настройки viewModel для заголовка блока продуктов
        fun PaymentNinjaTitle(text: String, visibility: Int) =
                BuyScreenTextViewModel(mText = text,
                        textColor = R.color.text_color_gray.getColor(),
                        textSize = R.dimen.buy_screen_title_text_size.getDimen().toInt(),
                        mTextVisibility = visibility,
                        paddingLeft = R.dimen.pay_reason_padding_left.getDimen() -
                                R.dimen.buy_screen_products_list_padding.getDimen(),
                        paddingTop = R.dimen.pay_reason_padding_top.getDimen(),
                        paddingRight = R.dimen.pay_reason_padding_right.getDimen(),
                        // сорян за такое значение падинга, так надо, чтобы не сломать старые экраны,
                        // но и сделать новый подобно им
                        paddingBottom = R.dimen.pay_reason_padding_bottom.getDimen() +
                                R.dimen.pay_reason_margin_bottom.getDimen() +
                                R.dimen.buy_screen_products_list_padding.getDimen())

        // Настройки viewModel для блока продуктов "Симпатии"
        fun PaymentNinjaLikesSection() = BuyScreenTextViewModel(
                mText = R.string.buying_sympathies.getString(),
                textColor = R.color.text_color_gray.getColor(),
                textSize = R.dimen.buy_screen_title_text_size.getDimen().toInt(),
                mTextVisibility = View.VISIBLE,
                paddingTop = R.dimen.buy_screen_products_likes_section_padding_top.getDimen(),
                paddingBottom = R.dimen.buy_screen_products_likes_section_padding_bottom.getDimen())

        // Настройки viewModel для блока продуктов "Монеты"
        fun PaymentNinjaCoinsSection() = BuyScreenTextViewModel(
                mText = R.string.buying_money.getString(),
                textColor = R.color.text_color_gray.getColor(),
                textSize = R.dimen.buy_screen_title_text_size.getDimen().toInt(),
                mTextVisibility = View.VISIBLE,
                paddingTop = R.dimen.buy_screen_products_coins_section_padding_top.getDimen(),
                paddingBottom = R.dimen.buy_screen_products_coins_section_padding_bottom.getDimen())

        // Заглушка для пустого списка продуктов
        fun PaymentNinjaPurchaseUnavailable() = BuyScreenTextViewModel(
                mText = R.string.general_buying_disabled.getString(),
                textColor = R.color.text_color_gray.getColor(),
                textSize = R.dimen.buy_screen_title_text_size.getDimen().toInt(),
                mTextVisibility = View.VISIBLE,
                marginBottom = R.dimen.buy_screen_products_unavailable_margins.getDimen(),
                marginLeft = R.dimen.buy_screen_products_unavailable_margins.getDimen(),
                marginRight = R.dimen.buy_screen_products_unavailable_margins.getDimen(),
                marginTop = R.dimen.buy_screen_products_unavailable_margins.getDimen())

        // title view для bottomSheet экрана "Платежи"
        fun PaymentNinjaBottomSheetTitle(text: String) = BuyScreenTextViewModel(
                mText = text,
                background = R.color.ninja_bottom_sheet_item_background,
                paddingBottom = R.dimen.payment_ninja_bottom_sheet_title_padding_bottom.getDimen(),
                paddingLeft = R.dimen.payment_ninja_bottom_sheet_title_padding_left.getDimen(),
                paddingTop = R.dimen.payment_ninja_bottom_sheet_title_padding_top.getDimen(),
                textColor = R.color.ninja_bottom_sheet_title.getColor(),
                textSize = R.dimen.payment_ninja_bottom_sheet_title_text_size.getDimen().toInt()
        )

        // item view для bottomSheet экрана "Платежи"
        fun PaymentNinjaBottomSheetItem(text: String, onClickListener: () -> Unit) = BuyScreenTextViewModel(
                mText = text,
                background = R.color.ninja_bottom_sheet_item_background,
                paddingBottom = R.dimen.payment_ninja_bottom_sheet_item_padding_bottom.getDimen(),
                paddingLeft = R.dimen.payment_ninja_bottom_sheet_item_padding_left.getDimen(),
                paddingTop = R.dimen.payment_ninja_bottom_sheet_item_padding_top.getDimen(),
                textColor = R.color.ninja_bottom_sheet_item.getColor(),
                textSize = R.dimen.payment_ninja_bottom_sheet_item_text_size.getDimen().toInt(),
                isAllCaps = true,
                onClickListener = onClickListener
        )
    }
}