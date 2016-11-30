package com.topface.topface.utils.glide_utils

import android.support.annotation.IntDef

/**
 *
 * Created by siberia87 on 30.11.16.
 */
object GlideTransformationType {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CropCircleType, AdmirationType, OnlineType, AdmirationOnlineType)
    annotation class GlideTransformationType

    const val CropCircleType = 0L
    const val AdmirationType = 1L
    const val OnlineType = 2L
    const val AdmirationOnlineType = 3L

}