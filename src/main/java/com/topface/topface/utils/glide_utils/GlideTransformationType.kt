package com.topface.topface.utils.glide_utils

import android.support.annotation.IntDef

/**
 *
 * Created by siberia87 on 30.11.16.
 */
object GlideTransformationType {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CROP_CIRCLE_TYPE, ADMIRATION_TYPE, ONLINE_TYPE, ADMIRATION_ONLINE_TYPE)
    annotation class GlideTransformationType

    const val CROP_CIRCLE_TYPE = 0L
    const val ADMIRATION_TYPE = 1L
    const val ONLINE_TYPE = 2L
    const val ADMIRATION_ONLINE_TYPE = 3L

}