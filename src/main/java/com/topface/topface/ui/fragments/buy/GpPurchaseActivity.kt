package com.topface.topface.ui.fragments.buy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.AcFragmentFrameBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.SingleFragmentActivity
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel
import com.topface.topface.ui.views.toolbar.view_models.EmptyToolbarViewModel
import com.topface.topface.utils.MarketApiManager
import com.topface.topface.utils.extensions.isSubscription
import org.onepf.oms.appstore.googleUtils.Purchase

class GpPurchaseActivity : SingleFragmentActivity<GpMarketFragment, AcFragmentFrameBinding>() {
    companion object {
        const val SKU_ID = "sku_id"
        const val FROM = "from"
        const val IS_SUBSCRIPTION = "is_subscription"
        const val ACTIVITY_REQUEST_CODE = 777
        const val FROM_DEFAULT = "UNDEFINED"
        const val PRODUCT = "success_purchased_product"

        // если не нашли skuid в списке продуктов, значит и покупку не будем инициировать
        fun getIntent(skuId: String, from: String): Intent {
            val isSubscription = skuId.isSubscription()
            return if (!MarketApiManager().isMarketApiAvailable || isSubscription == null) {
                Intent()
            } else {
                Intent(App.getContext(), GpPurchaseActivity::class.java).apply {
                    putExtra(SKU_ID, skuId)
                    putExtra(FROM, from)
                    putExtra(IS_SUBSCRIPTION, isSubscription)
                }
            }
        }
    }

    private lateinit var mSkuId: String
    private lateinit var mFrom: String
    private var mIsSubscription = false
    private val mFragment by lazy {
        ((supportFragmentManager.findFragmentByTag(GpMarketFragment::class.java.simpleName)
                as? GpMarketFragment) ?:
                GpMarketFragment.newInstance(mSkuId, mIsSubscription, mFrom).apply {
                    retainInstance = true
                }).apply {
            setOnPurchaseActions(object : GpMarketFragment.onPurchaseActions {
                override fun onPurchaseSuccess(product: Purchase?) {
                    closeWithSuccess(Intent().putExtra(PRODUCT, JsonUtils.toJson(product)))
                }

                override fun onPopupClosed() {
                    finish()
                }
            })
        }
    }

    override fun getFragmentTag(): String = GpMarketFragment::class.java.simpleName

    override fun createFragment() = mFragment

    override fun getToolbarBinding(binding: AcFragmentFrameBinding): ToolbarBinding = binding.toolbarInclude

    override fun getLayout() = R.layout.ac_fragment_frame

    override fun onCreate(savedInstanceState: Bundle?) {
        with(intent) {
            mSkuId = getStringExtra(SKU_ID)
            mFrom = getStringExtra(FROM) ?: FROM_DEFAULT
            mIsSubscription = getBooleanExtra(IS_SUBSCRIPTION, false)
        }
        super.onCreate(savedInstanceState)
    }

    override fun generateToolbarViewModel(toolbar: ToolbarBinding): BaseToolbarViewModel {
        return EmptyToolbarViewModel(toolbar)
    }

    private fun closeWithSuccess(intent: Intent? = null) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}