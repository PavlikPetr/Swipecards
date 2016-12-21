package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*

import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.topface.topface.R

/**
 * Этот transformation может рисовать восхищенеи на аватарке
 * Created by siberia87 on 30.11.16.
 */
class AdmirationTransformation(mContext: Context) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        super.transform(resource, outWidth, outHeight)
        drawTopCircleAdmiration()
        // этот super вызывается, чтобы верхние круги восхищений оказались под аватаркой
        super.transform(resource, outWidth, outHeight)
        drawHeardAdmiration()
        drawBottomCircleAdmiration()

        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    /*Тут верхние круги восхищений.*/
    private fun drawTopCircleAdmiration() {
        BitmapFactory.decodeResource(mContext.resources, R.drawable.circles_top).apply {
            val admirationCircleTop = Bitmap.createScaledBitmap(this, mMainBitmap.width, mMainBitmap.height, true)
            mCanvas.drawBitmap(admirationCircleTop, 0f, 0f, null)
            admirationCircleTop.recycle()
        }.recycle()
    }

    /*Тут круги для восхищений снизу*/
    private fun drawBottomCircleAdmiration() {
        BitmapFactory.decodeResource(mContext.resources, R.drawable.circles_bottom).apply {
            val admirationCircleBottom = Bitmap.createScaledBitmap(this, mMainBitmap.width, mMainBitmap.height, true)
            mCanvas.drawBitmap(admirationCircleBottom, 0f, 0f, null)
            admirationCircleBottom.recycle()
        }.recycle()
    }

    /*Тут рисуется сердечко.*/
    private fun drawHeardAdmiration() {
        BitmapFactory.decodeResource(mContext.resources, R.drawable.admiration_big_81).apply {
            val heart = Bitmap.createScaledBitmap(this, mMainBitmap.width, mMainBitmap.height, true)
            mCanvas.drawBitmap(heart, 0f, 0f, null)
            heart.recycle()
        }.recycle()
    }

    override fun getId() = "AdmirationTransformation"
}
