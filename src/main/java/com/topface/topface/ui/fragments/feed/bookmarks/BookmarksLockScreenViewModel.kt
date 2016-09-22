package com.topface.topface.ui.fragments.feed.bookmarks

import com.topface.topface.databinding.LayoutEmptyBookmarksBinding
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.viewModels.BaseViewModel

/**
 * Created by tiberal on 20.09.16.
 */
class BookmarksLockScreenViewModel(binding: LayoutEmptyBookmarksBinding, private val mNavigator: IFeedNavigator) :
        BaseViewModel<LayoutEmptyBookmarksBinding>(binding) {

    fun showDating() = mNavigator.showDating()

}