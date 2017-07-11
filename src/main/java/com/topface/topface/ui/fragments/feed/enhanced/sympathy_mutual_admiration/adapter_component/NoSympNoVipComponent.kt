package com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.adapter_component

import com.topface.topface.R
import com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.NoSympNoVipStub
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString

/**
 * Компонент дл стаба "У вас нет симпатий и нет Випа"
 */
class NoSympNoVipComponent(private val mFeedNavigator: FeedNavigator?) : BaseStubComponent<NoSympNoVipStub>() {

    companion object {
        const val TAG = "no_symphaties_no_vip_stub"
    }

    override val stubTitleText: String
        get() = R.string.you_have_not_sympathies.getString()
    override val stubText: String
        get() = R.string.become_a_vip_and_many_partners_will_see_you.getString()
    override val greenButtonText: String
        get() = R.string.chat_auto_reply_button.getString()
    override val borderlessButtonText: String
        get() = R.string.go_to_dating.getString()

    override fun greenButtonAction() {
        mFeedNavigator?.showPurchaseVip(TAG)
    }

    override fun onBorderlessButtonPress() {
        mFeedNavigator?.showDating()
    }
}