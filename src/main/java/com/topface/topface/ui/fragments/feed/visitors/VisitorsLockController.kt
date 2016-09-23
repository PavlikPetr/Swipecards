package com.topface.topface.ui.fragments.feed.visitors

import android.databinding.ViewStubProxy
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyVisitorsBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Created by tiberal on 09.09.16.
 */
class VisitorsLockController(stub: ViewStubProxy) :
        BaseFeedLockerController<LayoutEmptyVisitorsBinding, VisitorsLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
        mStubModel?.let {
            it.buttonText.set(App.getContext().getString(R.string.buying_vip_status))
            it.title.set(App.getContext().getString(R.string.with_vip_find_your_visitors))
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            it.buttonText.set(App.getContext().getString(R.string.general_get_dating))
            it.title.set(App.getContext().getString(R.string.go_dating_message))
        }
    }
}