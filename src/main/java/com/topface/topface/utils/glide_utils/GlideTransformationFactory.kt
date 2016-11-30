package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.Transformation

/**
 * Фабрика создания transformations
 * Created by siberia87 on 30.11.16.
 */
class GlideTransformationFactory(val mContext: Context) {
    fun construct(type: GlideTransformationType): Transformation<Bitmap> = when (type) {
        GlideTransformationType.CropCircleType -> CropCircleTransformation(mContext)
        GlideTransformationType.OnlineType -> OnlineTransformation(mContext)
        GlideTransformationType.AdmirationType -> AdmirationTransformation(mContext)
        GlideTransformationType.AdmirationOnlineType -> AdmirationOnlineTransformation(mContext)
        else -> CropCircleTransformation(mContext)
    }
}