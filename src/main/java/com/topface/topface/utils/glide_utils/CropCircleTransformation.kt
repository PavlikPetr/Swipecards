package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*

import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource

/**
 * Этот transformation может скруглять аватарку
 * Created by siberia87 on 25.11.16.
 */
class CropCircleTransformation(val mContext: Context) : Transformation<Bitmap> {

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

        return BitmapResource.obtain(bitmap, mBitmapPool)
    }

    override fun getId() = "CropCircleTransformation"

}
