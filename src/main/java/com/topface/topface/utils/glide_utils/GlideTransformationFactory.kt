package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.Transformation
import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * Фабрика создания transformations
 * Created by siberia87 on 30.11.16.
 */
class GlideTransformationFactory(val mContext: Context) {
    fun construct(@GlideTransformationType.GlideTransformationType type: Long, radiusOnline: Float?, outSideLine: Float?): Array<Transformation<Bitmap>> =
            when (type) {
                GlideTransformationType.ADMIRATION_TYPE -> arrayOf(AdmirationTransformation(mContext))

                GlideTransformationType.CROP_CIRCLE_TYPE -> arrayOf(
                        NewCropTransformation(mContext),
                        CropCircleTransformation(mContext))

                GlideTransformationType.ADMIRATION_AND_ONLINE_TYPE -> arrayOf(
                        NewCropTransformation(mContext),
                        CropCircleTransformation(mContext),
                        AdmirationTransformation(mContext),
                        NewOnlineTransformation(mContext, radiusOnline as Float, outSideLine as Float))

                GlideTransformationType.ONLINE_CIRCLE_TYPE -> arrayOf(
                        NewCropTransformation(mContext),
                        CropCircleTransformation(mContext),
                        NewOnlineTransformation(mContext, radiusOnline as Float, outSideLine as Float))

                else -> arrayOf(NewCropTransformation(mContext), CropCircleTransformation(mContext))
            }
}