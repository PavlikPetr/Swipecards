package com.topface.topface.ui.fragments.feed.fans

import android.databinding.ViewStubProxy
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyFansBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController


class FansLockController(stub: ViewStubProxy) :
        BaseFeedLockerController<LayoutEmptyFansBinding, FansLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
        mStubModel?.let {
            with(it) {
                buttonText.set(App.getContext().getString(R.string.buying_vip_status))
                title.set(App.getContext().getString(R.string.likes_buy_vip))
                setOnButtonClickListener(View.OnClickListener {
                    mNavigator.showPurchaseVip("Fans")
                })
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            with(it) {
                buttonText.set(App.getContext().getString(R.string.buy_sympathies))
                title.set(App.getContext().getString(R.string.buy_more_sympathies))
                setOnButtonClickListener(View.OnClickListener {
                    mNavigator.showPurchaseCoins("Fans")
                })
            }
        }
    }
}