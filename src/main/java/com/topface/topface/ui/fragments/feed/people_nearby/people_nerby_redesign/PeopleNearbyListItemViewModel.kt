package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.content.Context
import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.data.Profile
import com.topface.topface.databinding.PeopleNearbyListItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.glide_utils.GlideTransformationType
import com.topface.topface.viewModels.BaseViewModel

/**
 *  Вьюмодель для итема списка людей рядом
 */
class PeopleNearbyListItemViewModel(val item: FeedGeo, val navigator: FeedNavigator,
                                    private val mPopoverControl: IPopoverControl) {

    companion object {
        private const val PLC = "geo"
    }

    private val mFeedUser = item.user
    val avatar = ObservableField(mFeedUser.photo)
    val placeholderRes = ObservableField(if (item.user.sex == Profile.BOY) R.drawable.feed_banned_male_avatar else R.drawable.feed_banned_female_avatar)
    val type = GlideTransformationType.CROP_SQUARE_TYPE
    val nameAndAge = ObservableField(mFeedUser.nameAndAge)
    val distance = ObservableField(prepareDistanceText())
    val onlineImage = ObservableField(prepareImageForOnline())

    private fun prepareImageForOnline() = if (mFeedUser.online) R.drawable.online else 0

    private fun prepareDistanceText() =
            when {
                mFeedUser.deleted -> R.string.user_is_deleted.getString()
                mFeedUser.banned -> R.string.user_is_banned.getString()
                else -> prepareStringFromDouble()
            }

    private fun prepareStringFromDouble() =
            if (item.distance >= 1000) {
                String.format(R.string.general_distance_km.getString(), item.distance / 1000)
            } else {
                String.format(R.string.general_distance_m.getString(),
                        if (item.distance >= 1) item.distance.toInt() else 1)
            }

    fun onClick() {
        mPopoverControl.close()
        navigator.showProfile(item, PLC)
    }

}