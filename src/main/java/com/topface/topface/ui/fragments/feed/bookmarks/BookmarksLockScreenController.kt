package com.topface.topface.ui.fragments.feed.bookmarks

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyBookmarksBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Created by tiberal on 20.09.16.
 */
class BookmarksLockScreenController(stub: ViewStubProxy): BaseFeedLockerController<LayoutEmptyBookmarksBinding,BookmarksLockScreenViewModel>(stub) {
    override fun initLockedFeedStub(errorCode: Int) {

    }

    override fun initEmptyFeedStub() {
    }
}