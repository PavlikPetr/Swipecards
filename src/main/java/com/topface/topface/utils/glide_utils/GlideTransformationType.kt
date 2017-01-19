package com.topface.topface.utils.glide_utils

import android.support.annotation.IntDef

/**
 *
 * Created by siberia87 on 30.11.16.
 */
object GlideTransformationType {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CROP_CIRCLE_TYPE, ADMIRATION_TYPE, DIALOG_ONLINE_TYPE, ADMIRATION_AND_ONLINE_TYPE,CROP_SQUARE_TYPE)
    annotation class GlideTransformationType

    /**
     * @param CROP_CIRCLE_TYPE - параметр для выбора transition скругления аватарки
     * @param ADMIRATION_TYPE - параметр для выбора transition отрисовки рюшек восхищения
     * @param DIALOG_ONLINE_TRANSFORMATION_TYPE - параметр для выбора transition отрисовки значка онлайн на item'e диалогов
     * @param ADMIRATION_ONLINE_TRANSFORMATION_TYPE - параметр для выбора transition отрисовки значка онлайн на item'e восхищений
     * @param ADMIRATION_AND_ONLINE_TYPE - параметр для выбора transition отрисовки значка онлайн и восхищений
     * @param CROP_SQUARE_TYPE - параметр для выбора transition квадратного кропа картинки
     */

    const val CROP_CIRCLE_TYPE = 0L
    const val ADMIRATION_TYPE = 1L
    const val DIALOG_ONLINE_TYPE = 2L
    const val ADMIRATION_ONLINE_TYPE = 3L
    const val ADMIRATION_AND_ONLINE_TYPE = 4L
    const val CROP_SQUARE_TYPE = 5L
}