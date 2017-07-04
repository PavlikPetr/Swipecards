package com.topface.topface.ui.views.image_switcher

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.topface.topface.App
import com.topface.topface.state.EventBus

/**
 * VM для картинки альбома
 * Created by ppavlik on 15.02.17.
 */

class AlbumImageViewModel {
    private val mEventBus: EventBus by lazy {
        App.getAppComponent().eventBus()
    }
    val isProgressVisible = ObservableInt()

    val preloadedDrawable = ObservableField<GlideDrawable>()
    val isCropTopEnabled = ObservableBoolean(App.get().options.cropAndGalleryEnabled)
    fun onClick() = mEventBus.setData(ImageClick())
}