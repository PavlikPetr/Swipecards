package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration

import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.BaseLikesFeedViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class AdmirationItemViewModel(item: FeedBookmark, navigator: IFeedNavigator) :
        BaseLikesFeedViewModel<FeedBookmark>(item, navigator) {

    override val feed_type = "Admiration"
}