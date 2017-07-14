package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual

import android.databinding.ViewStubProxy
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class MutualLockController(stub: ViewStubProxy, private val mNavigator: IFeedNavigator) : BaseFeedLockerController<MutualLockScreenViewModel>(stub) {
    override fun initLockedFeedStub(errorCode: Int) {
        // нет блокировок
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            with(it) {
                greenButtonAction = { mNavigator.showDating() }
                onBorderlessButtonPress = { mNavigator.showVisitors() }
            }
        }
    }
}