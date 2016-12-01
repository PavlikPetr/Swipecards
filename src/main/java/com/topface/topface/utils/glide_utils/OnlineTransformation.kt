package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*

import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.topface.topface.R

/**
 * Этот transformation может рисовать значок онлайн на аватарке
 * Created by siberia87 on 30.11.16.
 */
class OnlineTransformation(mContext: Context) : BaseGlideTransformation(mContext){

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        super.transform(resource, outWidth, outHeight)

        /*Тут рисуется значок онлайн с обводкой*/
        var online = BitmapFactory.decodeResource(mContext.resources, R.drawable.online_big)
        online = Bitmap.createScaledBitmap(online, mMainBitmap.width, mMainBitmap.height, true)
        mCanvas.drawBitmap(online, 0f, 0f, null)
        online.recycle()

        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    override fun getId() = "OnlineTransformation"
}
