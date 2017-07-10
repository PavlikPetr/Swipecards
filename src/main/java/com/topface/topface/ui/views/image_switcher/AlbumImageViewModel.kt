package com.topface.topface.ui.views.image_switcher

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.topface.topface.App
import com.topface.topface.state.EventBus

/**
 * VM для картинки альбома
 * Created by ppavlik on 15.02.17.
 */

class AlbumImageViewModel(@ImageLoader.Companion.CropType val cropType: Long = ImageLoader.CROP_TYPE_NONE) {
    private val mEventBus: EventBus by lazy {
        App.getAppComponent().eventBus()
    }
    val isProgressVisible = ObservableInt()

    val preloadedDrawable = ObservableField<GlideDrawable>()
    fun onClick() = mEventBus.setData(ImageClick())
}