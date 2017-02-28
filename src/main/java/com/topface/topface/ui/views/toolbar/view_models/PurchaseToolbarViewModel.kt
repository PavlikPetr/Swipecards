package com.topface.topface.ui.views.toolbar.view_models

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.databinding.PurchaseToolbarAdditionalViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.toolbar_custom_view.PurchaseCustomToolbarViewModel
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Created by ppavlik on 18.10.16.
 * вьюмодель для тулбара экрана покупок
 */
class PurchaseToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding,
                                                         mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {
    private var balanceSubscription: Subscription
    private var additionalViewModel: PurchaseCustomToolbarViewModel

    init {
        title.set(R.string.purchase_header_title.getString())
        val additionalViewBinding = DataBindingUtil.inflate<PurchaseToolbarAdditionalViewBinding>(LayoutInflater.from(context),
                R.layout.purchase_toolbar_additional_view, null, false)
        additionalViewModel = PurchaseCustomToolbarViewModel(additionalViewBinding)
        additionalViewBinding.viewModel = additionalViewModel
        binding.toolbarCustomView.addView(additionalViewBinding.root)
        balanceSubscription = App.getAppComponent().appState().getObservable(BalanceData::class.java)
                .subscribe(shortSubscription {
                    it?.let {
                        additionalViewModel.coins.set(it.money.toString())
                        additionalViewModel.likes.set(it.likes.toString())
                    }
                })
    }

    override fun release() {
        super.release()
        balanceSubscription.safeUnsubscribe()
        additionalViewModel.release()
    }
}