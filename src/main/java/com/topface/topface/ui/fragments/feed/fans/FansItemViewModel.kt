package com.topface.topface.ui.fragments.feed.fans

import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.FeedItemSimpleLayoutBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by tiberal on 09.09.16.
 */
class FansItemViewModel(binding: FeedItemSimpleLayoutBinding, item: FeedBookmark, navigator: IFeedNavigator, isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedItemSimpleLayoutBinding, FeedBookmark>(binding, item, navigator, isActionModeEnabled) {
}