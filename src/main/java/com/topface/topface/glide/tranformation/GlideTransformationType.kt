package com.topface.topface.glide.tranformation

import android.support.annotation.IntDef

/**
 *
 * Created by siberia87 on 30.11.16.
 */
object GlideTransformationType {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CROP_CIRCLE_TYPE, ADMIRATION_TYPE, ADMIRATION_AND_ONLINE_TYPE, ONLINE_CIRCLE_TYPE, CIRCLE_AVATAR_WITH_STROKE_AROUND)
    annotation class GlideTransformation

    /**
     * @param CROP_CIRCLE_TYPE - параметр для выбора transition скругления аватарки
     * @param ADMIRATION_TYPE - параметр для выбора transition отрисовки рюшек восхищения
     * @param ADMIRATION_AND_ONLINE_TYPE - параметр для выбора transition отрисовки значка онлайн и восхищений
     * @param CROP_SQUARE_TYPE - параметр для выбора transition квадратного кропа картинки
     * @param CIRCLE_AVATAR_WITH_STROKE_AROUND - параметр для выбора Круглого аватара с обводкой вокруг
     */

    const val CROP_CIRCLE_TYPE = 0L
    const val ADMIRATION_TYPE = 1L
    const val ADMIRATION_AND_ONLINE_TYPE = 2L
    const val ONLINE_CIRCLE_TYPE = 3L
    const val CIRCLE_AVATAR_WITH_STROKE_AROUND = 4L
}