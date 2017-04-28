package com.topface.topface.ui.fragments.buy.pn_purchase.components

import com.topface.topface.R
import com.topface.topface.databinding.BuyButtonVer1Binding
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.views.BuyButtonVer1
import com.topface.topface.utils.extensions.appContext
import com.topface.topface.utils.extensions.getTag
import com.topface.topface.utils.extensions.getTitle

/**
 * Buy Paymnet Ninja product component
 * Created by petrp on 02.03.2017.
 */
class BuyButtonComponent(private val onClick: (data: PaymentNinjaProduct) -> Unit) : AdapterComponent<BuyButtonVer1Binding, PaymentNinjaProduct>() {
    override val itemLayout: Int
        get() = R.layout.buy_button_ver_1
    override val bindingClass: Class<BuyButtonVer1Binding>
        get() = BuyButtonVer1Binding::class.java

    override fun bind(binding: BuyButtonVer1Binding, data: PaymentNinjaProduct?, position: Int) {
        data?.let { product ->
            binding.handler = BuyButtonVer1.BuyButtonBuilder()
                    .discount(false)
                    .tag(product.getTag())
                    .showType(product.showType)
                    .title(product.title)
                    .onClick {
                        onClick(product)
                    }
                    .build(binding.appContext())
                    .mBtnHandler
        }
    }
}