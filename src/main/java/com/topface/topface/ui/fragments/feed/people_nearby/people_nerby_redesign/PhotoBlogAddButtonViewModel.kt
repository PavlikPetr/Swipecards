package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.data.User
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getDrawable
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription
import javax.inject.Inject

/**
 * VM итема постановки в фотоленту
 * Created by ppavlik on 11.01.17.
 */
class PhotoBlogAddButtonViewModel(private val mNavigator: IFeedNavigator, profile: Profile) : PhotoBlogItemViewModel(profile.photo,
        getPlaceholder(profile), R.drawable.place_in.getDrawable(), marginLeft = R.dimen.photoblog_add_button_margin_left.getDimen(), avatarClickListener = {
    if (App.get().profile.photo.isEmpty) {
        mNavigator.showTakePhotoPopup()
    } else {
        mNavigator.showAddToLeader()
    }
}) {
    @Inject lateinit var appState: TopfaceAppState
    private val mProfileSubscription: Subscription

    companion object {
        fun getPlaceholder(profile: Profile) = if (profile.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small
    }

    init {
        App.get().inject(this)
        mProfileSubscription = appState.getObservable(Profile::class.java)
                .subscribe(shortSubscription {
                    it?.let {
                        userPhoto.set(it.photo)
                        placeholder.set(getPlaceholder(it))
                    }
                })
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
    }
}
