package com.topface.topface.ui.fragments.feed.visitors

import com.topface.topface.data.Visitor
import com.topface.topface.databinding.FeedItemSimpleTimeBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator


class VisitorsItemViewModel(binding: FeedItemSimpleTimeBinding, item: Visitor, navigator: IFeedNavigator, isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedItemSimpleTimeBinding, Visitor>(binding, item, navigator, isActionModeEnabled) {

    var time: String = item.createdRelative

    override val text: String?
        get() = item.user?.city?.name

}