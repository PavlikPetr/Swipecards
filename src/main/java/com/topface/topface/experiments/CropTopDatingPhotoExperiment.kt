package com.topface.topface.experiments

import android.view.View
import com.topface.topface.App
import com.topface.topface.utils.extensions.loadBackground
import rx.Observable

/**
 * Decides:
 * 1. which background for dating photo to use blured or empty
 * 2. how crop photo fit in center or crop top/middle parts
 */
object CropTopDatingPhotoExperiment {
    fun getLoadBackgroundObservable(view: View, link: String) = if (App.get().options.cropAndGalleryEnabled) {
        Observable.empty()
    } else {
        view.loadBackground(link)
    }
}