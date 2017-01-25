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
        GlideTransformationType.CROP_SQUARE_TYPE -> CropSquareTransformation(mContext)
        GlideTransformationType.ADMIRATION_ONLINE_TYPE -> OnlineCircleTransformation(mContext, 14F, 17F)
        GlideTransformationType.ADMIRATION_TYPE -> AdmirationTransformation(mContext)
        GlideTransformationType.ADMIRATION_AND_ONLINE_TYPE -> AdmirationAndOnlineTransformation(mContext, 14F, 17F)
        GlideTransformationType.ONLINE_CIRCLE_TYPE -> OnlineCircleTransformation(mContext, 15F, 18F)
        else -> CropCircleTransformation(mContext)
    }
}