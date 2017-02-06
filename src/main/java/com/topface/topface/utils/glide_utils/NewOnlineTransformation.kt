package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.topface.topface.R
import com.topface.topface.utils.extensions.getColor

/**
 * Отрисовка(да-да, именно отрисовка) значка онлайн на округленном аватаре
 * Created by garastard on 25.01.17.
 */
open class NewOnlineTransformation(val mContext: Context, val circleRadius: Float, val strokeSize: Float) : Transformation<Bitmap> {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        drawOnlineCircle(resource,circleRadius,strokeSize)
        return resource
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
        val canvas = Canvas(resource.get())
        canvas.drawCircle(distanceToCircle, distanceToCircle, circleRadius + strokeSize, Paint().apply { color = R.color.circle_around_online_circle.getColor()
            isAntiAlias = true })
        canvas.drawCircle(distanceToCircle, distanceToCircle, circleRadius, Paint().apply { color = R.color.online_circle.getColor()
                isAntiAlias = true})
    }

    override fun getId() = "NewOnlineTransformation"
}
