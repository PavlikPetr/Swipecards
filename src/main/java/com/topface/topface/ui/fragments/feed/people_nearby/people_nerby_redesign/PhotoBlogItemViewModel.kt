package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.databinding.ObservableField
import android.databinding.ObservableLong
import android.graphics.drawable.Drawable
import com.topface.topface.data.Photo
import com.topface.topface.utils.glide_utils.GlideTransformationType

/**
 * VM итема фотоленты
 * Created by ppavlik on 11.01.17.
 */
open class PhotoBlogItemViewModel(photo: Photo?, placeholderRes: Int, val foregroundRes: Drawable? = null,
                                  val avatarClickListener: () -> Unit) {
    val userPhoto = ObservableField<Photo>()
    val placeholder = ObservableField(placeholderRes)
    val type = ObservableLong(GlideTransformationType.CROP_CIRCLE_TYPE)

    init {
        photo?.let {
            userPhoto.set(it)
        }
    }
}
