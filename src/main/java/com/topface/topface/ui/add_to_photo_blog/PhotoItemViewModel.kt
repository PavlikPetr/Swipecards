package com.topface.topface.ui.add_to_photo_blog

import android.databinding.ObservableField
import android.graphics.drawable.Drawable
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.state.EventBus
import com.topface.topface.utils.extensions.getDrawable
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import javax.inject.Inject

/**
 * View model for one photo in list of user photos in experimental add-to-photo-blog screen
 * Created by mbayutin on 12.01.17.
 */
class PhotoItemViewModel(photo: Photo) {
    @Inject lateinit var mEventBus: EventBus
    private var mPhotoSelectedSubscription: Subscription
    val userPhoto = ObservableField(photo)
    var foregroundDrawable = ObservableField<Drawable>()
    val placeholderRes = ObservableField<Int>(0)

    init {
        App.get().inject(this)
        mPhotoSelectedSubscription = mEventBus.getObservable(PhotoSelectedEvent::class.java)
                .subscribe { setSelected(photo.id == it.id)
                }
    }

    fun onClick() = mEventBus.setData(PhotoSelectedEvent(userPhoto.get().id))

    fun setSelected(selected: Boolean) = foregroundDrawable.set(if (selected) R.drawable.selected_photo.getDrawable() else null)

    fun release() = mPhotoSelectedSubscription.safeUnsubscribe()
}