package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.topface.topface.R

/**
 * Этот transformation для отрисовки значка online на item'e восхищений
 * Created by siberia87 on 13.12.16.
 */
class AdmirationOnlineTransformation(mContext: Context) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        super.transform(resource, outWidth, outHeight)
        /*Тут рисуется значок онлайн с обводкой*/
        drawOnline()
        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    fun drawOnline(): Bitmap? = BitmapFactory.decodeResource(mContext.resources, R.drawable.online_big).apply {
        val online = Bitmap.createScaledBitmap(this, mMainBitmap.width, mMainBitmap.height, true)
        mCanvas.drawBitmap(online, 0f, 0f, null)
        online.recycle()
    }

    override fun getId() = "AdmirationOnlineTransformation"
}
