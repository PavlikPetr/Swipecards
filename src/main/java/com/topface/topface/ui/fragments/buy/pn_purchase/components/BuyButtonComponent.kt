package com.topface.topface.ui.fragments.buy.pn_purchase.components

import android.text.TextUtils
import com.topface.topface.R
import com.topface.topface.data.BuyButtonData
import com.topface.topface.databinding.BuyButtonVer1Binding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.ui.views.BuyButtonVer1
import com.topface.topface.utils.extensions.appContext
import com.topface.topface.utils.extensions.getTag

/**
 * Buy Paymnet Ninja product component
 * Created by petrp on 02.03.2017.
 */
class BuyButtonComponent : AdapterComponent<BuyButtonVer1Binding, BuyButtonData>() {
    override val itemLayout: Int
        get() = R.layout.buy_button_ver_1
    override val bindingClass: Class<BuyButtonVer1Binding>
        get() = BuyButtonVer1Binding::class.java

    override fun bind(binding: BuyButtonVer1Binding, data: BuyButtonData?, position: Int) {
        data?.let {
            binding.handler = BuyButtonVer1.BuyButtonBuilder()
                    .discount(it.discount > 0)
                    .tag(it.getTag())
                    .showType(it.showType).title(it.title)
                    .onClick {
                    }
                    .build(binding.appContext())
                    .mBtnHandler
        }
    }
}