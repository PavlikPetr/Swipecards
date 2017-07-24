package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.ViewStubProxy
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.IApi
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getString

class LikesLockController(stub: ViewStubProxy, private val mNavigator: IFeedNavigator, private val mApi: IApi) : BaseFeedLockerController<LikesLockScreenViewModel>(stub) {
    override fun initLockedFeedStub(errorCode: Int) {
        mStubModel?.let {
            it.showChild.set(1)
            with(it.lockForMoneyViewModel) {
                mApi = this@LikesLockController.mApi
                buyVipAction = { mNavigator.showPurchaseCoins("EmptyLikes") }
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            it.showChild.set(0)
            with(it.emptyStubVM) {
                if (App.get().profile.premium) {
                    stubTitleText.set(R.string.you_have_not_sympathies.getString())
                    stubText.set(R.string.go_to_dating_and_rate_people.getString())
                    greenButtonText.set(R.string.go_to_dating.getString())
                    borderlessButtonText.set(R.string.go_to_guests.getString())
                    greenButtonAction = { mNavigator.showDating() }
                    onBorderlessButtonPress = { mNavigator.showVisitors() }
                } else {
                    stubTitleText.set(R.string.you_have_not_sympathies.getString())
                    stubText.set(R.string.become_a_vip_and_many_partners_will_see_you.getString())
                    greenButtonText.set(R.string.chat_auto_reply_button.getString())
                    borderlessButtonText.set(R.string.go_to_dating.getString())
                    greenButtonAction = { mNavigator.showPurchaseVip("no_symphaties_no_vip_stub") }
                    onBorderlessButtonPress = { mNavigator.showDating() }
                }
            }
        }
    }
}