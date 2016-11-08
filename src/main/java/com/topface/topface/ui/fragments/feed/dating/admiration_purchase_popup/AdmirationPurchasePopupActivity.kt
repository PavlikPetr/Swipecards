package com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup

import android.databinding.DataBindingUtil
import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.databinding.AdmirationPurchasePopupBinding
import com.topface.topface.ui.analytics.TrackedFragmentActivity
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils

/**
 * Это активити попата восхищения. Такие дела.
 * Created by siberia87 on 01.11.16.
 */
class AdmirationPurchasePopupActivity : TrackedFragmentActivity(), IAdmirationPurchasePopupVisible,
        IStartPurchaseScreenDelegate {

    companion object {
        const val RESULT_CODE_BOUGHT_VIP = 69
        const val RESULT_CODE_BOUGHT_COINS = 96
        const val CURRENT_USER = "current_user"
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<AdmirationPurchasePopupBinding>(layoutInflater, R.layout.admiration_purchase_popup, null, false)
    }

    private val mAdmirationPurchasePopupViewModel by lazy {
        AdmirationPurchasePopupViewModel(mBinding, mAdmirationPurchasePopupVisible = this, mStartPurchaseScreenDelegate = this,
                currentUser = intent.getParcelableExtra(CURRENT_USER))
    }

    private val mNavigator by lazy {
        FeedNavigator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
//      используется метод setViewModel() поскольку при работе с пропертей viewModel возникает ошибка: Unresolved reference
        mBinding.setViewModel(mAdmirationPurchasePopupViewModel)
        if (Utils.isLollipop()) {
            FabTransform.setup(this, mBinding.container)
        }
    }

    override fun onBackPressed() = super.onBackPressed()

    override fun hideAdmirationPurchasePopup(resultCode: Int) {
        setResult(resultCode)
        if (Utils.isLollipop()) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    override fun startCoinScreenPurchase() = mNavigator.showPurchaseCoins()

    override fun startVIPScreenPurchase() = mNavigator.showPurchaseVip()

    override fun onDestroy() {
        super.onDestroy()
        mAdmirationPurchasePopupViewModel.release()
    }
}
