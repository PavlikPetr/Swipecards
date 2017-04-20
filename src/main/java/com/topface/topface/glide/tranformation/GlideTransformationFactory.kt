package com.topface.topface.glide.tranformation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.bumptech.glide.load.Transformation
import com.topface.topface.utils.glide_utils.DrawCircleUnderAvatar
import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * Фабрика создания transformations
 * Created by siberia87 on 30.11.16.
 */
class GlideTransformationFactory(val mContext: Context) {
    fun construct(@GlideTransformationType.GlideTransformationType type: Long, radiusOnline: Float?, outSideLine: Float?, circleColor: Int = Color.WHITE): Array<Transformation<Bitmap>> =
            when (type) {
                GlideTransformationType.ADMIRATION_TYPE -> arrayOf(AdmirationTransformation(mContext))

                GlideTransformationType.CROP_CIRCLE_TYPE -> arrayOf(
                        CropAtImageViewTransformation(mContext),
                        CropCircleTransformation(mContext))

                GlideTransformationType.ADMIRATION_AND_ONLINE_TYPE -> arrayOf(
                        CropAtImageViewTransformation(mContext),
                        CropCircleTransformation(mContext),
                        AdmirationTransformation(mContext),
                        NewOnlineTransformation(mContext, radiusOnline as Float, outSideLine as Float))

                GlideTransformationType.ONLINE_CIRCLE_TYPE -> arrayOf(
                        CropAtImageViewTransformation(mContext),
                        CropCircleTransformation(mContext),
                        NewOnlineTransformation(mContext, radiusOnline as Float, outSideLine as Float))

                GlideTransformationType.CIRCLE_AVATAR_WITH_STROKE_AROUND -> arrayOf(
                        CropAtImageViewTransformation(mContext),
                        CropCircleTransformation(mContext),
                        DrawCircleUnderAvatar(mContext, outSideLine as Float, circleColor))

                else -> arrayOf(CropAtImageViewTransformation(mContext), CropCircleTransformation(mContext))
            }
}
