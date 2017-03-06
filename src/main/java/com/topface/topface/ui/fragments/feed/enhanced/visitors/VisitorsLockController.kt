package com.topface.topface.ui.fragments.feed.enhanced.visitors

import android.databinding.ViewStubProxy
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController

class VisitorsLockController(stub: ViewStubProxy) :
        BaseFeedLockerController<VisitorsLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
        mStubModel?.let {
            with(it) {
                buttonText.set(App.getContext().getString(R.string.buying_vip_status))
                title.set(App.getContext().getString(R.string.with_vip_find_your_visitors))
                setOnButtonClickListener(View.OnClickListener {
                    mNavigator.showPurchaseVip("Visitors")
                })
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            with(it) {
                buttonText.set(App.getContext().getString(R.string.general_get_dating))
                title.set(App.getContext().getString(R.string.go_dating_message))
                setOnButtonClickListener(View.OnClickListener {
                    mNavigator.showDating()
                })
            }
        }
    }
}