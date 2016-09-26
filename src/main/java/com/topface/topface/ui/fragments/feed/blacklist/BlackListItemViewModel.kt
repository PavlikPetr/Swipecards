package com.topface.topface.ui.fragments.feed.blacklist

import com.topface.topface.data.BlackListItem
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.BlackListItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.AgeAndNameData

class BlackListItemViewModel(binding: BlackListItemBinding, item: BlackListItem, mNavigator: IFeedNavigator, isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<BlackListItemBinding, BlackListItem>(binding, item, mNavigator, isActionModeEnabled) {

    override fun getNameAndAge(feedUser: FeedUser) = AgeAndNameData(feedUser.nameAndAge, null, 0)

    override val text: String?
        get() = item.user.city.name

}