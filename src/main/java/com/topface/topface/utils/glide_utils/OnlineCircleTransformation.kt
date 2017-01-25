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
class OnlineCircleTransformation(mContext: Context,val circleRadius: Float, val outsideCircleRadius: Float) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        super.transform(resource, outWidth, outHeight)
        drawOnlineCircle(resource, circleRadius, outsideCircleRadius)
        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
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

    override fun getId() = "DialogOnlineTransformation"
}
