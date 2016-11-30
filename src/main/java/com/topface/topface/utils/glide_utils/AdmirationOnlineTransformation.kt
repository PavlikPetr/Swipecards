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
 * Этот transformation может рисовать восхищение и значок online на аватарке
 * Created by siberia87 on 30.11.16.
 */
class AdmirationOnlineTransformation(mContext: Context) : BaseGlideTransformation(mContext) {

    val mAdmirationTransformation = AdmirationTransformation(mContext)

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        mAdmirationTransformation.transform(resource, outWidth, outHeight)
        val bitmap = Bitmap.createBitmap(mAdmirationTransformation.mMainBitmap.width, mAdmirationTransformation.mMainBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(mAdmirationTransformation.mMainBitmap, 0f, 0f, null)

        /*Тут рисуется значок онлайн с обводкой*/
        var online = BitmapFactory.decodeResource(mContext.resources, R.drawable.online_big)
        online = Bitmap.createScaledBitmap(online, mAdmirationTransformation.mMainBitmap.width, mAdmirationTransformation.mMainBitmap.height, true)
        canvas.drawBitmap(online, 0f, 0f, null)
        online.recycle()

        return BitmapResource.obtain(bitmap, mBitmapPool)
    }

    override fun getId() = "AdmirationOnlineTransformation"

}