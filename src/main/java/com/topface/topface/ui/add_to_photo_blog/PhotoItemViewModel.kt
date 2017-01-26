package com.topface.topface.ui.add_to_photo_blog

import android.databinding.Observable
import android.databinding.Observable.OnPropertyChangedCallback
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.drawable.Drawable
import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.utils.extensions.getDrawable

/**
 * View model for one photo in list of user photos in experimental add-to-photo-blog screen
 * Created by mbayutin on 12.01.17.
 */
class PhotoItemViewModel(photo: Photo, val lastSelectedPhotoId: ObservableInt) {
    val userPhoto = ObservableField(photo)
    var foregroundDrawable = ObservableField<Drawable>()
    val placeholderRes = ObservableInt(0)

    private val mOnPropertyChangedCallback: OnPropertyChangedCallback = object: OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) {
            (observable as ObservableInt)?.let { setSelected(it.get() == photo.id) }
        }
    }

    init {
        lastSelectedPhotoId.addOnPropertyChangedCallback(mOnPropertyChangedCallback)
        setSelected(lastSelectedPhotoId.get() == photo.id)
    }

    fun onClick() = lastSelectedPhotoId.set(userPhoto.get().id)

    fun setSelected(selected: Boolean) = foregroundDrawable.set(if (selected) R.drawable.selected_photo.getDrawable() else null)

    fun release() = lastSelectedPhotoId.removeOnPropertyChangedCallback(mOnPropertyChangedCallback)
}