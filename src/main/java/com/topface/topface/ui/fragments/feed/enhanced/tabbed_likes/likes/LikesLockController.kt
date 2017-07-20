package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.ViewStubProxy
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class LikesLockController(stub: ViewStubProxy, private val mNavigator: IFeedNavigator) : BaseFeedLockerController<LikesLockScreenViewModel>(stub) {
    override fun initLockedFeedStub(errorCode: Int) {
    }

    override fun initEmptyFeedStub() {
    }
}