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
class AdmirationAndOnlineTransformation(mContext: Context,val circleRadius: Float, val outsideCircleRadius: Float) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val mAdmirationTransformation = AdmirationTransformation(mContext)
        mAdmirationTransformation.transform(resource, outWidth, outHeight)
        val workBitmap = mAdmirationTransformation.mMainBitmap
        val width = workBitmap.width
        val height = workBitmap.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawBitmap(workBitmap, 0f, 0f, null)
            drawOnlineCircle(resource,circleRadius,outsideCircleRadius)
        }
        workBitmap.recycle()
        return BitmapResource.obtain(bitmap, mBitmapPool)
    }

    /**
     * Метод для отрисовки кружочка онлайн
     *
     * @param resource    аватар юзера, на котором будем рисовать
     * @param circleRadius радиус непосредственно онлайн кружочка
     * @param outsideCircleRadius    радиус кружочкка обводки
     */
    private fun drawOnlineCircle(resource: Resource<Bitmap>, circleRadius: Float, outsideCircleRadius: Float) {
        val radius = (resource.get().width / 2).toDouble()
        val distanceToCircle = (radius + Math.sqrt((radius * radius) / 2)).toFloat()
        mCanvas.drawCircle(distanceToCircle, distanceToCircle, outsideCircleRadius, Paint().apply { color = mContext.resources.getColor(R.color.circle_around_online_circle) })
        mCanvas.drawCircle(distanceToCircle, distanceToCircle, circleRadius, Paint().apply { color = mContext.resources.getColor(R.color.online_circle) })
    }

    override fun getId() = "AdmirationAndOnlineTransformation"
}