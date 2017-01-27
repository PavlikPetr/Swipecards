package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.Transformation
import com.topface.topface.R
import com.topface.topface.utils.extensions.getDimen

/**
 * Фабрика создания transformations
 * Created by siberia87 on 30.11.16.
 */
class GlideTransformationFactory(val mContext: Context) {
    fun construct(@GlideTransformationType.GlideTransformationType type: Long): Transformation<Bitmap> = when (type) {
        GlideTransformationType.CROP_CIRCLE_TYPE -> CropCircleTransformation(mContext)
        GlideTransformationType.ADMIRATION_TYPE -> AdmirationTransformation(mContext)
        GlideTransformationType.ADMIRATION_AND_ONLINE_TYPE -> AdmirationAndOnlineTransformation(mContext, R.dimen.dialog_online_circle.getDimen(), R.dimen.dialog_stroke_size.getDimen())
        GlideTransformationType.ONLINE_CIRCLE_TYPE -> OnlineCircleTransformation(mContext, R.dimen.dialog_online_circle.getDimen(), R.dimen.dialog_stroke_size.getDimen())
        else -> CropCircleTransformation(mContext)
    }
}