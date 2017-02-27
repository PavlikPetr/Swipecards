package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * Отрисовка кружка под аватаром
 * Created by garastard on 25.01.17.
 */
open class DrawCircleUnderAvatar(val mContext: Context, val outSideStrokeSize: Float) : Transformation<Bitmap> {

    protected val mBitmapPool: BitmapPool by lazy {
        Glide.get(mContext).bitmapPool
    }

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val resWidth = resource.get().width
        val mMainBitmap = mBitmapPool.get(resWidth, resWidth, Bitmap.Config.ARGB_8888) ?: Bitmap.createBitmap(resWidth, resWidth, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(mMainBitmap)
        val bitmapForCircle = Bitmap.createBitmap(resWidth, resWidth, Bitmap.Config.ARGB_8888)
        val canvFromRes = Canvas(bitmapForCircle)
        val radiusCircle = resWidth / 2.toFloat()
        canvFromRes.drawCircle(radiusCircle, radiusCircle, radiusCircle, Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
        })
        mCanvas.drawBitmap(bitmapForCircle, 0F, 0F, Paint().apply { isAntiAlias = true })
        bitmapForCircle.recycle()
        val sizeForAvatar = (resWidth - outSideStrokeSize).toInt()
        val croppedAvatar = CropCircleTransformation(mContext).transform(resource, sizeForAvatar, sizeForAvatar).get()
        val resizeAvatar = Bitmap.createScaledBitmap(croppedAvatar, sizeForAvatar, sizeForAvatar, true)
        mCanvas.drawBitmap(resizeAvatar, outSideStrokeSize/2, outSideStrokeSize/2, null)
        resizeAvatar.recycle()
        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    override fun getId() = "DrawUnderUserAvatar"

}
