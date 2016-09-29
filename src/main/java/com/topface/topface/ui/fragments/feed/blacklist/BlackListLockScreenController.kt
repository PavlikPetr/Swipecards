package com.topface.topface.ui.fragments.feed.blacklist

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyBlacklistBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

class BlackListLockScreenController(stub: ViewStubProxy) : BaseFeedLockerController<LayoutEmptyBlacklistBinding, BlackListLockScreenViewModel>(stub) {
    override fun initLockedFeedStub(errorCode: Int) {

    }

    override fun initEmptyFeedStub() {
    }
}