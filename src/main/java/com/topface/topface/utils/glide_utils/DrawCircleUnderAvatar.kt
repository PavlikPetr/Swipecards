package com.topface.topface.utils.glide_utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.topface.topface.R
import com.topface.topface.ui.views.ImageViewRemote.DEFAULT_BORDER_COLOR
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getDimen

/**
 * Отрисовка кружка под аватаром
 * Created by garastard on 25.01.17.
 */
open class DrawCircleUnderAvatar(val mContext: Context, val outSideStrokeSize: Float) : Transformation<Bitmap> {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        getRoundBitmap(resource.get(), R.dimen.popup_message_side_padding.getDimen()/2, R.dimen.mutual_popup_stroke_outside.getDimen(), null)
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
//        val neccessarySize = (resourceWidth + strokeSize).toInt()
//        val workPlace = Bitmap.createScaledBitmap(inBitmap,neccessarySize,neccessarySize,false)
        val workPlace = Bitmap.createBitmap(resourceWidth, resourceWidth, Bitmap.Config.ARGB_8888)
        val actualCenter = (workPlace.width/2).toFloat()
        val canvas = Canvas(workPlace)

        canvas.drawCircle(actualCenter, actualCenter, actualCenter, Paint().apply {
            color = R.color.circle_around_online_circle.getColor()
            isAntiAlias = true
        })
        canvas.drawBitmap(inBitmap,0f, 0f, null)
    }

    fun getRoundBitmap(bitmap: Bitmap, radiusMult: Float, borderWidth: Float, borderColor: ColorStateList?): Bitmap {
        var borderColor = borderColor
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val whiteColor = Color.WHITE
        borderColor = if (borderColor != null) borderColor else DEFAULT_BORDER_COLOR

        val mask = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvasMask = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = whiteColor
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvasMask.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvasMask.drawCircle((bitmapWidth / 2).toFloat(), (bitmapHeight / 2).toFloat(), bitmapWidth / 2 - borderWidth, paint)

        val output = mask.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(output)
        val borderSize = borderWidth.toInt()
        paint.reset()
        paint.isAntiAlias = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        val scaledImageHeight = ((bitmapHeight - borderSize) / radiusMult).toInt()
        val scaledImageWidth = ((bitmapWidth - borderSize) / radiusMult).toInt()
        val scaledImagePaddingHorizontal = (bitmapWidth - scaledImageWidth) / 2
        val scaledImagePaddingVertical = (bitmapHeight - scaledImageWidth) / 2
        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, scaledImageWidth, scaledImageHeight, true), scaledImagePaddingHorizontal.toFloat(), scaledImagePaddingVertical.toFloat(), paint)
        paint.reset()
        paint.isAntiAlias = true
        paint.color = borderColor!!.defaultColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        canvas.drawCircle((bitmapWidth / 2).toFloat(), (bitmapHeight / 2).toFloat(), bitmapWidth / 2 - borderWidth / 2, paint)
        return output
    }


    override fun getId() = "DrawUnderUserAvatar"
}
