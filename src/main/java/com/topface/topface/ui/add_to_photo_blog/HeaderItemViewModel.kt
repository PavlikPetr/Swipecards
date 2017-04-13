package com.topface.topface.ui.add_to_photo_blog

import android.databinding.Observable
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.data.Photo
import com.topface.topface.utils.extensions.getPlaceholderRes

/**
 * View model for header item
 * Created by mbayutin on 11.01.17.
 */
class HeaderItemViewModel(val lastSelectedPhotoId: ObservableInt) {
    var photo = ObservableField<Photo>()
    val placeholderRes = ObservableInt(0)

    private val mOnPropertyChangedCallback: Observable.OnPropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) {
            (observable as ObservableInt)?.let {  setPhotoById(it.get()) }
        }
    }


    init {
        placeholderRes.set(App.get().profile.getPlaceholderRes())
        lastSelectedPhotoId.addOnPropertyChangedCallback(mOnPropertyChangedCallback)
        setPhotoById(lastSelectedPhotoId.get())
    }

    private fun setPhotoById(id: Int) = photo.set(if (id > 0) App.get().profile.photos.find{ it.id == id } else Photo())

    fun release() = lastSelectedPhotoId.removeOnPropertyChangedCallback(mOnPropertyChangedCallback)
}