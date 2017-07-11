package com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.adapter_component

import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.NoSympButVipStub
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString

/**
 * Компонент дл стаба "У вас нет симпатий но есть вип"
 */
class NoSympButVipComponent(private val mFeedNavigator: FeedNavigator?): BaseStubComponent<NoSympButVipStub>() {

    override val stubTitleText: String
        get() = R.string.you_have_not_sympathies.getString()
    override val stubText: String
        get() = R.string.go_to_dating_and_rate_people.getString()
    override val greenButtonText: String
        get() = R.string.go_to_dating.getString()
    override val borderlessButtonText: String
        get() = R.string.go_to_guests.getString()

    override fun greenButtonAction() {
        mFeedNavigator?.showDating()
    }

    override fun onBorderlessButtonPress() {
        mFeedNavigator?.showVisitors()
    }
}