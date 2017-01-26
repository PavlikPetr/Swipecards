package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*

import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.topface.topface.R

/**
 * Отрисовка(да-да, именно отрисовка) значка онлайн на округленном аватаре
 * Created by garastard on 25.01.17.
 */
class OnlineCircleTransformation(mContext: Context, val circleRadius: Float, val strokeSize: Float) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        super.transform(resource, outWidth, outHeight)
        drawOnlineCircle(resource, circleRadius, strokeSize)
        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    override fun getId() = "DialogOnlineTransformation"
}
