package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.adapter_component

import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.NoMutualsStub
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString

/**
 * Компонент дл стаба "У вас нет симпатий и нет Випа"
 */
class NoMutualsComponent(private val mFeedNavigator: FeedNavigator?) : BaseStubComponent<NoMutualsStub>() {

    override val stubTitleText = R.string.mutual_no_mutuals.getString()
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