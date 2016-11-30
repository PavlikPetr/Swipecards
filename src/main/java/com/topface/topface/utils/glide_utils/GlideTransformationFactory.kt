package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.Transformation

/**
 * Фабрика создания transformations
 * Created by siberia87 on 30.11.16.
 */
class GlideTransformationFactory(val mContext: Context) {
    fun construct(@GlideTransformationType.GlideTransformationType type: Long): Transformation<Bitmap> = when (type) {
        GlideTransformationType.CROP_CIRCLE_TYPE -> CropCircleTransformation(mContext)
        GlideTransformationType.ONLINE_TYPE -> OnlineTransformation(mContext)
        GlideTransformationType.ADMIRATION_TYPE -> AdmirationTransformation(mContext)
        GlideTransformationType.ADMIRATION_OLINE_TYPE -> AdmirationOnlineTransformation(mContext)
        else -> CropCircleTransformation(mContext)
    }
}