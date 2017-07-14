package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by ppavlik on 13.07.17.
 * базовая viewmodel для карточного вида взаимных фидов и восхищений
 */
open class BaseLikesFeedViewModel<out T : FeedItem>(private val mItem: T, private val mNavigator: IFeedNavigator) : BaseViewModel() {
    open val feed_type: String = "UNDEFINED"

    val placeholderRes = ObservableField(if (mItem.user.sex == Profile.BOY) R.drawable.rounded_nearby_av_man else R.drawable.rounded_nearby_av_girl)
    val avatar = ObservableField(mItem.user?.photo)
    val onlineImage = ObservableInt(if (mItem.user?.online ?: false) R.drawable.im_list_online else 0)
    val name = ObservableField(mItem.user?.nameAndAge ?: "")
    val city = ObservableField(mItem.user?.city?.name ?: "")

    open fun onItemClick() {
        mNavigator.showProfile(mItem, feed_type.toLowerCase())
    }

    fun showChat() = mNavigator.showChat(mItem, feed_type.toLowerCase())
}