package com.topface.topface.ui.fragments.feed.bookmarks

import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.FeedBookmarksItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by tiberal on 19.09.16.
 */
class BookmarksItemViewModel(binding: FeedBookmarksItemBinding, item: FeedBookmark, mNavigator: IFeedNavigator, isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedBookmarksItemBinding, FeedBookmark>(binding, item, mNavigator, isActionModeEnabled) {

    override val text: String?
        get() = item.user.city.name

}