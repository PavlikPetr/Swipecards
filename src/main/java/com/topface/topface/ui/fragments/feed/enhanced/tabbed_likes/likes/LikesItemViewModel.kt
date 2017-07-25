package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.ObservableField
import android.databinding.ObservableFloat
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.data.FeedLike
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel

/**
 * Created by ppavlik on 17.07.17.
 * Вью-модель итема симпатии в виде карточки, по аналогии с tinder
 */

class LikesItemViewModel(data: FeedBookmark) : BaseViewModel(), IViewModel<FeedBookmark> {
    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    val avatar = ObservableField(getAvatar(data))
    val placeholderRes = ObservableField(getPlaceholder(data))
    val name = ObservableField(getName(data))
    val city = ObservableField(getCity(data))
    val backgroundAlpha = ObservableFloat(1f)

    fun onLikeClick() {
        mEventBus.setData(LikesCardUserChoose(true))
        backgroundAlpha.set(0f)
    }

    fun onSkipClick() {
        mEventBus.setData(LikesCardUserChoose(false))
        backgroundAlpha.set(0f)
    }

    override fun update(data: FeedBookmark) {
        avatar.set(getAvatar(data))
        placeholderRes.set(getPlaceholder(data))
        name.set(getName(data))
        city.set(getCity(data))
    }

    private fun getAvatar(data: FeedBookmark) = data.user?.photo?.defaultLink

    private fun getPlaceholder(data: FeedBookmark) = if (data.user?.sex == Profile.BOY) R.drawable.nearby_av_man else R.drawable.nearby_av_girl

    private fun getName(data: FeedBookmark) = data.user?.nameAndAge ?: ""

    private fun getCity(data: FeedBookmark) = data.user?.city?.name ?: ""
}
