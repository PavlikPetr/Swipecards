package com.topface.topface.ui.fragments.feed.enhanced.fans

import com.topface.topface.data.FeedBookmark
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator


class FansItemViewModel(item: FeedBookmark, navigator: IFeedNavigator, isActionModeEnabled: () -> Boolean, click: () -> Unit) :
        BaseFeedItemViewModel<FeedBookmark>(item, navigator, isActionModeEnabled, click) {

    override val feed_type: String
        get() = "Fans"

    override val text: String?
        get() = item.user?.city?.name

    override val time: String?
        get() = ""
}