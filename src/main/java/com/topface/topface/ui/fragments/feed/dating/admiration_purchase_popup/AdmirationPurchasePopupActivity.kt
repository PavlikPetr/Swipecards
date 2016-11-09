package com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup

import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.databinding.AdmirationPurchasePopupBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.analytics.TrackedFragmentActivity
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel
import com.topface.topface.ui.views.toolbar.view_models.InvisibleToolbarViewModel
import com.topface.topface.utils.Utils

/**
 * Это активити попата восхищения. Такие дела.
 * Created by siberia87 on 01.11.16.
 */
class AdmirationPurchasePopupActivity : TrackedFragmentActivity<AdmirationPurchasePopupBinding>(), IAdmirationPurchasePopupHide {

    override fun getToolbarBinding(binding: AdmirationPurchasePopupBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.admiration_purchase_popup

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) = InvisibleToolbarViewModel(toolbar)

    companion object {
        const val INTENT_ADMIRATION_PURCHASE_POPUP = 69
        const val CURRENT_USER = "current_user"
    }

    private val mAdmirationPurchasePopupViewModel by lazy {
        AdmirationPurchasePopupViewModel(viewBinding, mAdmirationPurchasePopupHide = this, mNavigator = mNavigator,
                currentUser = intent.getParcelableExtra(CURRENT_USER))
    }

    private val mNavigator by lazy {
        FeedNavigator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
//      используется метод setViewModel() поскольку при работе с пропертей viewModel возникает ошибка: Unresolved reference
        viewBinding.setViewModel(mAdmirationPurchasePopupViewModel)

        if (Utils.isLollipop()) {
            FabTransform.setup(this, viewBinding.container)
        }
    }

    override fun hideAdmirationPurchasePopup(resultCode: Int) {
        setResult(resultCode)
        if (Utils.isLollipop()) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdmirationPurchasePopupViewModel.release()
    }
}
