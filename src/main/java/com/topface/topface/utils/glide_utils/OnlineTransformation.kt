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
class OnlineTransformation(val mContext: Context) : Transformation<Bitmap> {

    val mBitmapPool: BitmapPool by lazy {
        Glide.get(mContext).bitmapPool
    }

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val source = resource.get()
        val size = Math.min(source.width, source.height)
        val width = (source.width - size) / 2
        val height = (source.height - size) / 2
        val bitmap = mBitmapPool.get(size, size, Bitmap.Config.ARGB_8888) ?: Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val shaderAvatar = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            if (width != 0 || height != 0) {
                with(Matrix()) {
                    this@with.setTranslate(-width.toFloat(), -height.toFloat())
                    this@apply.setLocalMatrix(this@with)
                }
            }
        }
        val paint = with(Paint()) {
            shader = shaderAvatar
            isAntiAlias = true
            this@with
        }
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)

        /*Тут рисуется значок онлайн с обводкой*/
        var online = BitmapFactory.decodeResource(mContext.resources, R.drawable.online_big)
        online = Bitmap.createScaledBitmap(online, size, size, true)
        canvas.drawBitmap(online, 0f, 0f, null)

        return BitmapResource.obtain(bitmap, mBitmapPool)
    }

    override fun getId() = "OnlineTransformation"
}
