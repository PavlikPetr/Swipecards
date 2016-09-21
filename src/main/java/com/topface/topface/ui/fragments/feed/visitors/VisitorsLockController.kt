package com.topface.topface.ui.fragments.feed.visitors

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyVisitorsBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Created by tiberal on 09.09.16.
 */
class VisitorsLockController(stub: ViewStubProxy):
        BaseFeedLockerController<LayoutEmptyVisitorsBinding, VisitorsLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
    }

    override fun initEmptyFeedStub() {
    }
}