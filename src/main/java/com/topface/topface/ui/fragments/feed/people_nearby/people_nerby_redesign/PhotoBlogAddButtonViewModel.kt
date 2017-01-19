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
class PhotoBlogAddButtonViewModel(private val mNavigator: IFeedNavigator, profile: Profile, popoverControl: IPopoverControl) {
    @Inject lateinit var appState: TopfaceAppState
    private val mProfileSubscription: Subscription
    val photoBlogViewModel: PhotoBlogItemViewModel

    private fun getPlaceholder(profile: Profile) =
            if (profile.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small

    init {
        App.get().inject(this)
        photoBlogViewModel = PhotoBlogItemViewModel(profile.photo,
                getPlaceholder(profile), R.drawable.place_in.getDrawable(),
                marginLeft = R.dimen.photoblog_add_button_margin_left.getDimen(),
                avatarClickListener = {
                    popoverControl.close(true)
                    if (App.get().profile.photo.isEmpty) {
                        mNavigator.showTakePhotoPopup()
                    } else {
                        mNavigator.showAddToLeader()
                    }
                })
        mProfileSubscription = appState.getObservable(Profile::class.java)
                .subscribe(shortSubscription {
                    it?.let {
                        photoBlogViewModel.userPhoto.set(it.photo)
                        photoBlogViewModel.placeholder.set(getPlaceholder(it))
                    }
                })
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
    }
}
