package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getString

class BuyVipStubViewModel(private val mFeedUser: FeedUser, private val mFeedNavigator: FeedNavigator) {

    val isMan = mFeedUser.sex == User.BOY
    val title = ObservableField<String>(getTitle(mFeedUser))
    val stubText = ObservableField<String>(if (isMan) R.string.write_to_her_only_vip.getString() else R.string.write_to_him_only_vip.getString())

    val mutualUserPhoto = ObservableField(mFeedUser.photo)
    val mutualPlaceholderRes = ObservableField(if (isMan) R.drawable.dialogues_av_man_big else R.drawable.dialogues_av_girl_big)
    val outsideCircle = R.dimen.mutual_popup_stroke_outside.getDimen()
    val type = GlideTransformationType.CROP_CIRCLE_TYPE

    private fun getTitle(mFeedUser: FeedUser) = String.format(
            if (isMan) R.string.chat_buy_vip_popular_male.getString() else R.string.chat_buy_vip_popular_female.getString(), mFeedUser.firstName);

    fun buyVip() = mFeedNavigator.showPurchaseVip("chat_stub")

}