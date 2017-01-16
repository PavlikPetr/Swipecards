package com.topface.topface.ui.fragments.feed.people_nearby

import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.data.Profile
import com.topface.topface.databinding.PeopleNearbyListItemBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString
import com.topface.topface.viewModels.BaseViewModel

/**
 *  Вьюмодель для итема списка людей рядом
 */
class PeopleNearbyListItemViewModel(binding: PeopleNearbyListItemBinding, val item: FeedGeo, val navigator: FeedNavigator) : BaseViewModel<PeopleNearbyListItemBinding>(binding) {

    private val feedUser = item.user
    val avatar = ObservableField(prepareAvatar())
    val nameAndAge = ObservableField(feedUser.nameAndAge)
    val distance = ObservableField(prepareDistanceText())
    val onlineImage = ObservableField(prepareImageForOnline())

    private fun prepareImageForOnline() = if (feedUser.online) R.drawable.online else 0

    private fun prepareDistanceText() =
            when {
                feedUser.deleted -> R.string.user_is_deleted.getString()
                feedUser.banned -> R.string.user_is_banned.getString()
                else -> prepareStringFromDouble()
            }

    private fun prepareStringFromDouble() =
            if (item.distance >= 1000) {
                String.format(context.getString(R.string.general_distance_km), item.distance / 1000)
            } else {
                String.format(context.getString(R.string.general_distance_m), if (item.distance >= 1) item.distance.toInt() else 1)
            }

    private fun prepareAvatar() =
            when {
                feedUser.deleted || feedUser.banned || feedUser.photo.isEmpty || feedUser.photo == null -> if (feedUser.sex == Profile.BOY) R.drawable.feed_banned_male_avatar.getString()
                else R.drawable.feed_banned_female_avatar.getString()
                else -> feedUser.photo.defaultLink
            }

    fun onClick() = navigator.showProfile(item)

}