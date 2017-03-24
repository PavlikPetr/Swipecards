package com.topface.topface.ui.fragments.feed.enhanced.fans

import android.databinding.ViewStubProxy
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class FansLockController(stub: ViewStubProxy, private val mNavigator: IFeedNavigator) : BaseFeedLockerController<FansLockScreenViewModel>(stub) {
    override fun initLockedFeedStub(errorCode: Int) {
        mStubModel?.let {
            with(it) {
                buttonText.set(com.topface.topface.App.getContext().getString(com.topface.topface.R.string.buying_vip_status))
                title.set(com.topface.topface.App.getContext().getString(com.topface.topface.R.string.likes_buy_vip))
                setOnButtonClickListener(android.view.View.OnClickListener {
                    mNavigator.showPurchaseVip("Fans")
                })
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            with(it) {
                buttonText.set(com.topface.topface.App.getContext().getString(com.topface.topface.R.string.buy_sympathies))
                title.set(com.topface.topface.App.getContext().getString(com.topface.topface.R.string.buy_more_sympathies))
                setOnButtonClickListener(android.view.View.OnClickListener {
                    mNavigator.showPurchaseCoins("Fans")
                })
            }
        }
    }
}