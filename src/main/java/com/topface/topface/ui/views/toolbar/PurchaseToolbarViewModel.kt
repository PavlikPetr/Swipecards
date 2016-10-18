package com.topface.topface.ui.views.toolbar

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.databinding.PurchaseToolbarAdditionalViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.utils.extensions.getString

/**
 * Created by ppavlik on 18.10.16.
 * вьюмодель для тулбара экрана покупок
 */
class PurchaseToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding,
                                                         mNavigation: IToolbarNavigation? = null)
: BaseToolbarViewModel(binding, mNavigation) {
    var additionalViewBinding: PurchaseToolbarAdditionalViewBinding

    init {
        title.set(R.string.purchase_header_title.getString())
        additionalViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.purchase_toolbar_additional_view, null, false)
        binding.toolbarCustomView.addView(additionalViewBinding.root)
    }

    fun setCoinsCount(count: String) {
        additionalViewBinding.coins.text = count
    }

    fun setLikesCount(count: String) {
        additionalViewBinding.likes.text = count
    }

    fun setBalance(balance: BalanceData?) {
        balance?.let {
            setCoinsCount(it.money.toString())
            setLikesCount(it.likes.toString())
        }
    }
}