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
class AdmirationAndOnlineTransformation(mContext: Context) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val mAdmirationTransformation = AdmirationTransformation(mContext)
        mAdmirationTransformation.transform(resource, outWidth, outHeight)
        val workBitmap = mAdmirationTransformation.mMainBitmap
        val width = workBitmap.width
        val height = workBitmap.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawBitmap(workBitmap, 0f, 0f, null)
            drawOnline(this, width, height)
        }
        workBitmap.recycle()
        return BitmapResource.obtain(bitmap, mBitmapPool)
    }

    fun drawOnline(canvas: Canvas, width: Int, height: Int) {
        BitmapFactory.decodeResource(mContext.resources, R.drawable.online_big).apply {
            val online = Bitmap.createScaledBitmap(this, width, height, true)
            canvas.drawBitmap(online, 0f, 0f, null)
            online.recycle()
        }.recycle()
    }

    override fun getId() = "AdmirationAndOnlineTransformation"
}