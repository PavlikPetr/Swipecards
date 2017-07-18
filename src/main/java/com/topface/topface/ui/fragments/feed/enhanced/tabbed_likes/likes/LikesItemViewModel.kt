package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.data.FeedLike
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel

/**
 * Created by ppavlik on 17.07.17.
 * Вью-модель итема симпатии в виде карточки, по аналогии с tinder
 */

class LikesItemViewModel(data: FeedBookmark) : BaseViewModel() {
    val avatar = ObservableField(data.user?.photo?.defaultLink)
    val placeholderRes = ObservableField(if (data.user?.sex == Profile.BOY) R.drawable.nearby_av_man else R.drawable.nearby_av_girl)
    val name = ObservableField(data.user?.nameAndAge ?: "")
    val city = ObservableField(data.user?.city?.name ?: "")
}
