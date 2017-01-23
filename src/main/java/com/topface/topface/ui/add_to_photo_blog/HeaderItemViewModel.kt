package com.topface.topface.ui.add_to_photo_blog

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.data.Photo
import com.topface.topface.state.EventBus
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import javax.inject.Inject

/**
 * View model for header item
 * Created by mbayutin on 11.01.17.
 */
class HeaderItemViewModel {
    @Inject lateinit var mEventBus: EventBus
    private var mPhotoSelectedSubscription: Subscription
    var photo = ObservableField<Photo>()
    val placeholderRes = ObservableInt(0)

    init {
        App.get().inject(this)
        mPhotoSelectedSubscription = mEventBus.getObservable(PhotoSelectedEvent::class.java)
                .subscribe { event ->
                    photo.set(App.get().profile.photos.find{ it.id == event.id })
                }
    }

    fun release() = mPhotoSelectedSubscription.safeUnsubscribe()
}