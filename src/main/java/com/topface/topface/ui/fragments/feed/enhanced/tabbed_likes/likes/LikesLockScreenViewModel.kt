package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.ObservableInt
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs.BaseSympathyStubViewModel
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs.LockForMoneyViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked

class LikesLockScreenViewModel (mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel() {
    val showChild = ObservableInt(0)
    val emptyStubVM = BaseSympathyStubViewModel(mIFeedUnlocked)
    val lockForMoneyViewModel = LockForMoneyViewModel(mIFeedUnlocked)
}