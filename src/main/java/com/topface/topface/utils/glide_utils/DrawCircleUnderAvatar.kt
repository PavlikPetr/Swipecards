package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.topface.topface.R
import com.topface.topface.utils.extensions.getColor

/**
 * Отрисовка кружка под аватаром
 * Created by garastard on 25.01.17.
 */
open class DrawCircleUnderAvatar(val mContext: Context, val outSideStrokeSize: Float) : Transformation<Bitmap> {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        drawCircleUnderAvatar(resource, outSideStrokeSize)
        return resource
    }

    /**
     * Метод для отрисовки кружочка онлайн
     *
     * @param resource    аватар юзера, под которы будем рисовать
     * @param outSideStrokeSize    радиус кружочкка обводки
     */
    fun drawCircleUnderAvatar(resource: Resource<Bitmap>, strokeSize: Float) {

        val inBitmap = resource.get()
        val resourceWidth = inBitmap.width
        val neccessarySize = (resourceWidth + strokeSize).toInt()
        val workPlace = Bitmap.createScaledBitmap(inBitmap,neccessarySize,neccessarySize,false)
        val actualCenter = (workPlace.width/2).toFloat()
        val canvas = Canvas(workPlace)

        canvas.drawCircle(actualCenter, actualCenter, actualCenter, Paint().apply {
            color = R.color.circle_around_online_circle.getColor()
            isAntiAlias = true
        })
        canvas.drawBitmap(inBitmap,0f, 0f, null)
    }

    override fun getId() = "DrawUnderUserAvatar"
}
