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
 * Базовый transformation. Умеет загруглять аватарку
 * Created by siberia87 on 30.11.16.
 */
abstract class BaseGlideTransformation(context: Context) : Transformation<Bitmap> {

    protected val mContext: Context

    init {
        mContext = context.applicationContext
    }

    protected lateinit var mRemoteBitmap: Bitmap
    protected val mBitmapPool: BitmapPool by lazy {
        Glide.get(mContext).bitmapPool
    }
    internal val mMainBitmap by lazy {
        val minSize = Math.min(mRemoteBitmap.width, mRemoteBitmap.height)
        mBitmapPool.get(minSize, minSize, Bitmap.Config.ARGB_8888) ?: Bitmap.createBitmap(minSize, minSize, Bitmap.Config.ARGB_8888)
    }
    internal val mCanvas by lazy {
        Canvas(mMainBitmap)
    }

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        mRemoteBitmap = resource.get()
        val size = Math.min(mRemoteBitmap.width, mRemoteBitmap.height)
        val width = (mRemoteBitmap.width - size) / 2
        val height = (mRemoteBitmap.height - size) / 2
        val shaderAvatar = BitmapShader(mRemoteBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
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
        val radius = size / 2f
        mCanvas.drawCircle(radius, radius, radius, paint)

        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    /**
     * Метод для отрисовки кружочка онлайн
     *
     * @param resource    аватар юзера, на котором будем рисовать
     * @param circleRadius радиус непосредственно онлайн кружочка
     * @param strokeSize    радиус кружочкка обводки
     */
    fun drawOnlineCircle(resource: Resource<Bitmap>, circleRadius: Float, strokeSize: Float) {
        val radius = (resource.get().width / 2).toDouble()
        val distanceToCircle = (radius + Math.sqrt((radius * radius) / 2)).toFloat()
        mCanvas.drawCircle(distanceToCircle, distanceToCircle, circleRadius + strokeSize, Paint().apply { color = R.color.circle_around_online_circle.getColor() })
        mCanvas.drawCircle(distanceToCircle, distanceToCircle, circleRadius, Paint().apply { color = R.color.online_circle.getColor() })
    }


}
