package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration

import android.databinding.ViewStubProxy
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class AdmirationLockController(stub: ViewStubProxy, private val mNavigator: IFeedNavigator) : BaseFeedLockerController<AdmirationLockScreenViewModel>(stub) {
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