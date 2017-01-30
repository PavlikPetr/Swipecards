package com.topface.topface.utils.glide_utils

import android.support.annotation.IntDef

/**
 *
 * Created by siberia87 on 30.11.16.
 */
object GlideTransformationType {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CROP_CIRCLE_TYPE, ADMIRATION_TYPE, ADMIRATION_AND_ONLINE_TYPE,ONLINE_CIRCLE_TYPE)
    annotation class GlideTransformationType

    /**
     * @param CROP_CIRCLE_TYPE - параметр для выбора transition скругления аватарки
     * @param ADMIRATION_TYPE - параметр для выбора transition отрисовки рюшек восхищения
     * @param ADMIRATION_AND_ONLINE_TYPE - параметр для выбора transition отрисовки значка онлайн и восхищений
     * @param CROP_SQUARE_TYPE - параметр для выбора transition квадратного кропа картинки
     */

    const val CROP_CIRCLE_TYPE = 0L
    const val ADMIRATION_TYPE = 1L
    const val ADMIRATION_AND_ONLINE_TYPE = 4L
    const val ONLINE_CIRCLE_TYPE = 6L

}