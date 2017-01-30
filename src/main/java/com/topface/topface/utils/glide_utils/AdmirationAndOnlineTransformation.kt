package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.topface.topface.R
import com.topface.topface.utils.extensions.getColor

/**
 * Этот transformation может рисовать восхищение и значок online на аватарке
 * Created by siberia87 on 30.11.16.
 */
class AdmirationAndOnlineTransformation(mContext: Context, circleRadius: Float, strokeSize: Float) : OnlineCircleTransformation(mContext, circleRadius, strokeSize) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val mAdmirationTransformation = AdmirationTransformation(mContext)
        mAdmirationTransformation.transform(resource, outWidth, outHeight)
        val workBitmap = mAdmirationTransformation.mMainBitmap
        val width = workBitmap.width
        val height = workBitmap.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawBitmap(workBitmap, 0f, 0f, null)
            drawOnlineCircle(resource, circleRadius, strokeSize)
        }
        workBitmap.recycle()
        return BitmapResource.obtain(bitmap, mBitmapPool)
    }

    override fun getId() = "AdmirationAndOnlineTransformation"
}