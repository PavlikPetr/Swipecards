package com.topface.billing.ninja

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.topface.billing.ninja.fragments.AddCardFragment
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinjaAddCardBinding
import com.topface.topface.databinding.ToolbarViewBinding
import com.topface.topface.ui.BaseFragmentActivity
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.views.toolbar.view_models.EmptyToolbarViewModel

/**
 * Add bank card activity
 * Created by m.bayutin on 02.03.17.
 */
class NinjaAddCardActivity : BaseFragmentActivity<LayoutNinjaAddCardBinding>() {
    companion object {
        const val EXTRA_FROM_INSTANT_PURCHASE = "NinjaAddCardActivity.Extra.FromInstantPurchase"
        const val EXTRA_IS_TEST_PURCHASE = "NinjaAddCardActivity.Extra.IsTestPurchase"
        const val EXTRA_BUY_PRODUCT = "NinjaAddCardActivity.Extra.BuyProduct"
        const val EXTRA_SOURCE = "NinjaAddCardActivity.Extra.Source"
        const val REQUEST_CODE = 231
        const val CARD_SENDED_SUCCESFULL = "NinjaAddCardActivity.CardSendedSuccesfull"
        const val UNKNOWN_PLACE = "unknown_place"

        fun createIntent(fromInstantPurchase: Boolean, product: PaymentNinjaProduct? = null,
                         source: String, isTestPurchase: Boolean) =
                Intent(App.getContext(), NinjaAddCardActivity::class.java).apply {
                    putExtra(EXTRA_FROM_INSTANT_PURCHASE, fromInstantPurchase)
                    putExtra(EXTRA_IS_TEST_PURCHASE, isTestPurchase)
                    product?.let { putExtra(EXTRA_BUY_PRODUCT, it) }
                    putExtra(EXTRA_SOURCE, source.takeIf(String::isNotEmpty) ?: UNKNOWN_PLACE)
                }
    }

    private val mAddCardFragment by lazy {
        (supportFragmentManager.findFragmentByTag(AddCardFragment.TAG)
                as? AddCardFragment) ?: AddCardFragment.newInstance(intent.extras)
    }

//    private val mThreeDSecureFragment by lazy {
//        (supportFragmentManager.findFragmentByTag(ThreeDSecureFragment.TAG)
//                as? ThreeDSecureFragment) ?: ThreeDSecureFragment.newInstance(intent.extras)
//    }

    override fun getToolbarBinding(binding: LayoutNinjaAddCardBinding): ToolbarViewBinding = binding.toolbarInclude.apply { root?.visibility = View.GONE }

    override fun generateToolbarViewModel(toolbar: ToolbarViewBinding) = EmptyToolbarViewModel(toolbar)

    override fun getLayout(): Int = R.layout.layout_ninja_add_card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragment(mAddCardFragment, AddCardFragment.TAG)
    }

    fun addFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_content, fragment, tag).commit()
    }
}