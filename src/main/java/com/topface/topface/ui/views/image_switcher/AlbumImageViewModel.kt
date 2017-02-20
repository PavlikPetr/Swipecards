package com.topface.topface.ui.views.image_switcher

import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.state.EventBus
import javax.inject.Inject

/**
 * VM для картинки альбома
 * Created by ppavlik on 15.02.17.
 */

class AlbumImageViewModel {
    private val mEventBus: EventBus by lazy {
        App.getAppComponent().eventBus()
    }
    val isProgressVisible = ObservableInt()

    fun onClick() = mEventBus.setData(ImageClick())
}