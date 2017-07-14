package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration

import android.databinding.ViewStubProxy
import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getString

class AdmirationLockController(stub: ViewStubProxy, private val mNavigator: IFeedNavigator) : BaseFeedLockerController<AdmirationLockScreenViewModel>(stub) {
    override fun initLockedFeedStub(errorCode: Int) {
        mStubModel?.let {
            with(it) {
                buttonText.set(R.string.buying_vip_status.getString())
                title.set(R.string.likes_buy_vip.getString())
                setOnButtonClickListener(android.view.View.OnClickListener {
                    mNavigator.showPurchaseVip("Admiration")
                })
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            with(it) {
                buttonText.set(R.string.buy_sympathies.getString())
                title.set(R.string.buy_more_sympathies.getString())
                setOnButtonClickListener(android.view.View.OnClickListener {
                    mNavigator.showPurchaseCoins("Admiration")
                })
            }
        }
    }
}