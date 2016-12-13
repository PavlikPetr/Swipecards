package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*

import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.topface.topface.R

/**
 * Этот transformation для отрисовки значка online на item'е диалога
 * Created by siberia87 on 30.11.16.
 */
class DialogOnlineTransformation(mContext: Context) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        super.transform(resource, outWidth, outHeight)
        /*Тут рисуется значок онлайн с обводкой*/
        drawOnline()
        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    fun drawOnline() {
        BitmapFactory.decodeResource(mContext.resources, R.drawable.online_small).apply {
            val online = Bitmap.createScaledBitmap(this, mMainBitmap.width, mMainBitmap.height, true)
            mCanvas.drawBitmap(online, 0f, 0f, null)
            online.recycle()
        }.recycle()
    }

    override fun getId() = "DialogOnlineTransformation"
}
