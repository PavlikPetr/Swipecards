package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.feed_utils.AgeAndNameData
import com.topface.topface.ui.fragments.feed.feed_utils.AvatarHolder
import com.topface.topface.viewModels.BaseViewModel

/**
 * Базовая моделька для итема фидов. Может аватарку, имя и онлайн. Может Уйти в профиль
 * Created by tiberal on 19.08.16.
 */
open class BaseFeedItemViewModel<T : ViewDataBinding, out D : FeedItem>(binding: T, val item: D, private val mNavigator: IFeedNavigator,
                                                                        private val mIsActionModeEnabled: () -> Boolean) : BaseViewModel<T>(binding) {

    var avatarHolder: AvatarHolder? = null
    var nameAndAge: AgeAndNameData? = null
    open val text: String? = null

    init {
        item.user?.let {
            avatarHolder = AvatarHolder(it.photo, getStubResourсe())
            nameAndAge = AgeAndNameData(it.nameAndAge, null, getOnlineRes(it))
        }
    }

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
            if (mIsActionModeEnabled.invoke()) {
                getClickListenerForMultiselectHandle()?.forEach {
                    it.onClick(binding.root)
                }
                onAvatarClickActionModeEnabled()
            } else {
                onAvatarClickActionModeDisabled()
            }

    /**
     * Получить клик листенеры вьюх, которые при включенном экшен моде должны выделять итем при клике на нее
     */
    open fun getClickListenerForMultiselectHandle(): Array<View.OnClickListener>? = null

    open fun onAvatarClickActionModeEnabled() {
    }

    open fun onAvatarClickActionModeDisabled() = mNavigator.showProfile(item)

}