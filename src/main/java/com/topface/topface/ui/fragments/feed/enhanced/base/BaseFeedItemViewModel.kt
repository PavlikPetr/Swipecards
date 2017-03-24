package com.topface.topface.ui.fragments.feed.enhanced.base

import android.text.Html
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.AgeAndNameData
import com.topface.topface.ui.fragments.feed.feed_utils.AvatarHolder
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId

open class BaseFeedItemViewModel<out T : FeedItem>(val item: T, private val mNavigator: IFeedNavigator,
                                                   private val mIsActionModeEnabled: () -> Boolean, val click: () -> Unit) : BaseViewModel() {
    open val feed_type: String = "UNDEFINED"
    var avatarHolder: AvatarHolder? = null
    var nameAndAge: AgeAndNameData? = null
    open val text: String? = null
    open val time: String? = null

    companion object {
        const val AGE_TEMPLATE = ", %d"
        const val DOTS = "&#8230;"
        const val TAG_TEMPLATE = "%s_%d_%s"
    }

    init {
        item.user?.let {
            avatarHolder = AvatarHolder(it.photo, getStubResourсe())
            nameAndAge = getNameAndAge(it)
        }
    }

    fun getTag() = String.format(App.getCurrentLocale(), TAG_TEMPLATE, item.id, item.getUserId(), feed_type)

    open fun getNameAndAge(feedUser: FeedUser) = AgeAndNameData(
            if (TextUtils.isEmpty(feedUser.firstName))
                Html.fromHtml(DOTS).toString()
            else
                feedUser.firstName,
            String.format(App.getCurrentLocale(), AGE_TEMPLATE, feedUser.age), getOnlineRes(feedUser))

    private fun getOnlineRes(user: FeedUser): Int {
        if (user.state == null) {
            return 0
        }
        val isOnline = !(user.deleted || user.banned) && user.state.online
        return when (user.state.deviceType) {
            FeedUser.State.UNKNOWN -> 0
            FeedUser.State.MOBILE -> if (isOnline) R.drawable.ic_mobile_on else R.drawable.ic_mobile_off
            FeedUser.State.DESKTOP -> if (isOnline) R.drawable.ic_desktop_on else R.drawable.ic_desktop_off
            else -> 0
        }
    }

    private fun getStubResourсe() = if (item.user.sex == Profile.BOY)
        R.drawable.feed_banned_male_avatar
    else
        R.drawable.feed_banned_female_avatar


    fun onAvatarClick() =
            if (mIsActionModeEnabled()) {
                onAvatarClickActionModeEnabled()
            } else {
                onAvatarClickActionModeDisabled()
            }

    open fun onAvatarClickActionModeEnabled() {
        click()
    }

    open fun onAvatarClickActionModeDisabled() {
        mNavigator.showProfile(item, feed_type.toLowerCase())
    }

}