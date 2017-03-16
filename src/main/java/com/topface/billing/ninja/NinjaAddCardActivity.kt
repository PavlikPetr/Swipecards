package com.topface.billing.ninja

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinjaAddCardBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.BaseFragmentActivity
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.views.toolbar.view_models.EmptyToolbarViewModel

/**
 * Add bank card activity
 * Created by m.bayutin on 02.03.17.
 */
class NinjaAddCardActivity: BaseFragmentActivity<LayoutNinjaAddCardBinding>() {
    companion object {
        const val EXTRA_FROM_INSTANT_PURCHASE = "NinjaAddCardActivity.Extra.FromInstantPurchase"
        const val EXTRA_BUY_PRODUCT = "NinjaAddCardActivity.Extra.BuyProduct"
        const val REQUEST_CODE = 231

        private const val TAG = "NinjaAddCardActivity::"
        fun createIntent(fromInstantPurchase: Boolean, product: PaymentNinjaProduct? = null) = Intent(App.getContext(), NinjaAddCardActivity::class.java).apply {
            putExtra(EXTRA_FROM_INSTANT_PURCHASE, fromInstantPurchase)
            product?.let { putExtra(EXTRA_BUY_PRODUCT, it) }
        }
    }

    override fun getToolbarBinding(binding: LayoutNinjaAddCardBinding) = binding.toolbarInclude.apply { root?.visibility = View.GONE }

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) = EmptyToolbarViewModel(toolbar)

    override fun getLayout(): Int = R.layout.layout_ninja_add_card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            viewBinding.viewModel = AddCardViewModel(it.extras).setFeedNavigator(FeedNavigator(this))
        }
    }

}