package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual

import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs.BaseSympathyStubViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.extensions.getString

class MutualLockScreenViewModel(mIFeedUnlocked: IFeedUnlocked) :
        BaseSympathyStubViewModel(mIFeedUnlocked) {
    init {
        stubTitleText.set(R.string.mutual_no_mutuals.getString())
        stubText.set(R.string.go_to_dating_and_rate_people.getString())
        greenButtonText.set(R.string.go_to_dating.getString())
        borderlessButtonText.set(R.string.go_to_guests.getString())
    }
}