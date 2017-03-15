package com.topface.topface.ui.fragments.feed.enhanced.visitors

import com.topface.topface.api.responses.Visitor
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class VisitorsItemViewModel(item: Visitor, navigator: IFeedNavigator, isActionModeEnabled: () -> Boolean, click: () -> Unit) :
        BaseFeedItemViewModel<Visitor>(item, navigator, isActionModeEnabled, click) {

    override val feed_type: String
        get() = "Visitors"

    override val time: String?
        get() = item.createdRelative

    override val text: String?
        get() = item.user?.city?.name
}