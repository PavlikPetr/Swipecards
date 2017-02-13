package com.topface.topface.ui.views.image_switcher

import android.support.annotation.IntDef
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType

/**
 * Статусы процесса загрузки фото в альбом
 * Created by petrp on 09.02.2017.
 */

object Status {
    const val UNDEFINED = 0L
    const val NOT_LOADED = 1L
    const val START_PRELOAD = 2L
    const val PRELOAD_SUCCESS = 3L
    const val PRELOAD_FAILED = 4L
    const val LOAD_SUCCESS = 5L
    const val LOAD_FAILED = 6L
    const val START_LOAD = 7L
    const val SET_IMAGE_ON_SUCCESS_PRELOAD = 8L
    const val ALBUM_REQUEST_SENDED = 9L

    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
    @IntDef(UNDEFINED, NOT_LOADED, START_PRELOAD, PRELOAD_SUCCESS, PRELOAD_FAILED, LOAD_SUCCESS,
            LOAD_FAILED, START_LOAD, SET_IMAGE_ON_SUCCESS_PRELOAD, ALBUM_REQUEST_SENDED)
    annotation class ImageLoaderStatus
}