package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.getString

/**
 *  Вьюмодель для итема списка людей рядом
 */
class PeopleNearbyListItemViewModel(private val mItem: FeedGeo, private val mNavigator: FeedNavigator,
                                    private val mPopoverControl: IPopoverControl) {

    companion object {
        private const val PLC = "geo"
    }

    private val mFeedUser = mItem.user
    val avatar = ObservableField(prepareAvatar())
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
            if (mItem.distance >= 1000) {
                String.format(R.string.general_distance_km.getString(), mItem.distance / 1000)
            } else {
                String.format(R.string.general_distance_m.getString(),
                        if (mItem.distance >= 1) mItem.distance.toInt() else 1)
            }

    private fun prepareAvatar() =
            when {
                mFeedUser.deleted || mFeedUser.banned || mFeedUser.photo.isEmpty || mFeedUser.photo == null -> if (mFeedUser.sex == Profile.BOY) R.drawable.feed_banned_male_avatar.getString()
                else R.drawable.feed_banned_female_avatar.getString()
                else -> mFeedUser.photo.defaultLink
            }

    fun onClick() {
        mPopoverControl.close()
        mNavigator.showProfile(mItem, PLC)
    }
}