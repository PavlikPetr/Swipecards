package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.adapter_component

import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.NoAdmirationsStub
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString

/**
 * Компонент дл стаба "У вас нет симпатий и нет Випа"
 */
class NoAdmirationsComponent(private val mFeedNavigator: FeedNavigator?) : BaseStubComponent<NoAdmirationsStub>() {

    override val stubTitleText = R.string.you_have_not_admirations.getString()
    override val stubText = R.string.go_to_dating_and_rate_people.getString()
    override val greenButtonText = R.string.go_to_dating.getString()
    override val borderlessButtonText = R.string.go_to_guests.getString()

    override fun greenButtonAction() {
        mFeedNavigator?.showDating()
    }

    override fun onBorderlessButtonPress() {
        mFeedNavigator?.showVisitors()
    }
}