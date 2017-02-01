package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.extensions.getDrawable
import com.topface.topface.utils.extensions.getPlaceholderRes
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

    init {
        App.get().inject(this)
        photoBlogViewModel = PhotoBlogItemViewModel(profile.photo,
                profile.getPlaceholderRes(), R.drawable.place_in.getDrawable(), {
            popoverControl.closeByUser()
            if (!App.getConfig().userConfig.isUserAvatarAvailable &&
                    with(App.get().profile.photo) { this == null || isEmpty }) {
                mNavigator.showTakePhotoPopup()
            } else {
                mNavigator.showAddToLeader()
            }
        })
        mProfileSubscription = appState.getObservable(Profile::class.java)
                .subscribe(shortSubscription {
                    it?.let { profile ->
                        photoBlogViewModel.userPhoto.set(profile.photo?.let {
                            if (!it.isEmpty && !it.isFake) it else null
                        })
                        photoBlogViewModel.placeholder.set(it.getPlaceholderRes())
                    }
                })
    }

    fun release() {
        mProfileSubscription.safeUnsubscribe()
    }
}
