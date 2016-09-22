package com.topface.topface.ui.fragments.feed.fans

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyFansBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController


class FansLockController(stub: ViewStubProxy) :
        BaseFeedLockerController<LayoutEmptyFansBinding, FansLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
    }

    override fun initEmptyFeedStub() {
    }
}