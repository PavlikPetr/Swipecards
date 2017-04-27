package com.topface.billing.ninja

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.topface.billing.ninja.fragments.ThreeDSecureFragment
import com.topface.billing.ninja.fragments.add_card.AddCardFragment
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinjaAddCardBinding
import com.topface.topface.databinding.ToolbarViewBinding
import com.topface.topface.ui.BaseFragmentActivity
import com.topface.topface.ui.fragments.buy.pn_purchase.PaymentNinjaProduct
import com.topface.topface.ui.views.toolbar.view_models.EmptyToolbarViewModel
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Add bank card activity
 * Created by m.bayutin on 02.03.17.
 */
class NinjaAddCardActivity : BaseFragmentActivity<LayoutNinjaAddCardBinding>() {
    companion object {
        const val EXTRA_FROM_INSTANT_PURCHASE = "NinjaAddCardActivity.Extra.FromInstantPurchase"
        const val EXTRA_IS_TEST_PURCHASE = "NinjaAddCardActivity.Extra.IsTestPurchase"
        const val EXTRA_IS_3DS_PURCHASE = "NinjaAddCardActivity.Extra.Is3DSPurchase"
        const val EXTRA_BUY_PRODUCT = "NinjaAddCardActivity.Extra.BuyProduct"
        const val EXTRA_SOURCE = "NinjaAddCardActivity.Extra.Source"
        const val REQUEST_CODE = 231
        const val CARD_SENDED_SUCCESFULL = "NinjaAddCardActivity.CardSendedSuccesfull"
        const val UNKNOWN_PLACE = "unknown_place"
        const val SHOW_ADD_CARD = 0
        const val SHOW_3DS = 1
        private const val FRAGMENT_TYPE = "NinjaAddCardActivity.Show.FragmentType"

        fun createIntent(settings: ThreeDSecureParams) =
                Intent(App.getContext(), NinjaAddCardActivity::class.java).apply {
                    putExtra(ThreeDSecureFragment.EXTRA_SETTINGS, settings)
                    putExtra(FRAGMENT_TYPE, SHOW_3DS)
                }

        fun createIntent(fromInstantPurchase: Boolean, product: PaymentNinjaProduct? = null,
                         source: String, isTestPurchase: Boolean, is3DSPurchase: Boolean) =
                Intent(App.getContext(), NinjaAddCardActivity::class.java).apply {
                    putExtra(EXTRA_FROM_INSTANT_PURCHASE, fromInstantPurchase)
                    putExtra(EXTRA_IS_TEST_PURCHASE, isTestPurchase)
                    putExtra(EXTRA_IS_3DS_PURCHASE, is3DSPurchase)
                    product?.let { putExtra(EXTRA_BUY_PRODUCT, it) }
                    putExtra(EXTRA_SOURCE, source.takeIf(String::isNotEmpty) ?: UNKNOWN_PLACE)
                    putExtra(FRAGMENT_TYPE, SHOW_ADD_CARD)
                }
    }

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var m3DSecureSubscription: Subscription? = null

    override fun getToolbarBinding(binding: LayoutNinjaAddCardBinding): ToolbarViewBinding = binding.toolbarInclude.apply { root?.visibility = View.GONE }

    override fun generateToolbarViewModel(toolbar: ToolbarViewBinding) = EmptyToolbarViewModel(toolbar)

    override fun getLayout(): Int = R.layout.layout_ninja_add_card

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m3DSecureSubscription = mEventBus.getObservable(ThreeDSecureParams::class.java)
                .applySchedulers()
                .subscribe(shortSubscription {
                    it?.let {
                        addFragment(get3DSecureFragment(it), ThreeDSecureFragment.TAG)
                    }
                })
        when (intent.getIntExtra(FRAGMENT_TYPE, SHOW_ADD_CARD)) {
            SHOW_3DS -> addFragment(get3DSecureFragment(), ThreeDSecureFragment.TAG)
            else -> addFragment(getAddCardFragment(), AddCardFragment.TAG)
        }
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_content, fragment, tag).commit()
    }

    private fun get3DSecureFragment() =
            (supportFragmentManager.findFragmentByTag(ThreeDSecureFragment.TAG)
                    as? ThreeDSecureFragment) ?: ThreeDSecureFragment.newInstance(intent.extras)

    private fun get3DSecureFragment(settings: ThreeDSecureParams) =
            (supportFragmentManager.findFragmentByTag(ThreeDSecureFragment.TAG)
                    as? ThreeDSecureFragment) ?: ThreeDSecureFragment.newInstance(settings)

    private fun getAddCardFragment() =
            (supportFragmentManager.findFragmentByTag(AddCardFragment.TAG)
                    as? AddCardFragment) ?: AddCardFragment.newInstance(intent.extras)
}