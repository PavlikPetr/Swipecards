package com.topface.topface.ui.fragments.feed.photoblog

import android.databinding.ObservableField
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

class HeaderPhotoBlogViewModel(private val mNavigator: IFeedNavigator) {
    val urlAvatar: ObservableField<String> = ObservableField(Utils.getLocalResUrl(R.drawable.upload_photo_female))
    var profileSubscription: Subscription

    init {
        setUrlAvatar(App.get().profile)
        profileSubscription = App.getAppComponent().appState().getObservable(Profile::class.java)
                .subscribe(shortSubscription { it?.let { setUrlAvatar(it) } })
    }

    fun showAddToLeader() =
            if (!App.getConfig().userConfig.isUserAvatarAvailable && App.get().profile.photo == null) {
                mNavigator.showTakePhotoPopup()
            } else {
                mNavigator.showAddToLeader()
            }

    fun setUrlAvatar(profile: Profile) {
        val photoUrl = if (profile.photo != null) profile.photo.defaultLink else Utils.EMPTY
        urlAvatar.set(if (TextUtils.isEmpty(photoUrl))
            Utils.getLocalResUrl(if (profile.sex == Profile.BOY)
                R.drawable.upload_photo_male
            else
                R.drawable.upload_photo_female)
        else
            photoUrl)
    }
}