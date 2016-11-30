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

        /*Тут верхние круги восхищений.*/
        var admirationCircleTop = BitmapFactory.decodeResource(mContext.resources, R.drawable.circles_top)
        admirationCircleTop = Bitmap.createScaledBitmap(admirationCircleTop, mMainBitmap.width, mMainBitmap.height, true)
        mCanvas.drawBitmap(admirationCircleTop, 0f, 0f, null)
        admirationCircleTop.recycle()

        super.transform(resource, outWidth, outHeight)

        /*Тут рисуется сердечко.*/
        var admiration = BitmapFactory.decodeResource(mContext.resources, R.drawable.admiration_big_81)
        admiration = Bitmap.createScaledBitmap(admiration, mMainBitmap.width, mMainBitmap.height, true)
        mCanvas.drawBitmap(admiration, 0f, 0f, null)
        admiration.recycle()

        /*Тут круги доя восхищений снизу*/
        var admirationCircleBottom = BitmapFactory.decodeResource(mContext.resources, R.drawable.circles_bottom)
        admirationCircleBottom = Bitmap.createScaledBitmap(admirationCircleBottom,  mMainBitmap.width, mMainBitmap.height, true)
        mCanvas.drawBitmap(admirationCircleBottom, 0f, 0f, null)
        admirationCircleBottom.recycle()

        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

    override fun getId() = "AdmirationTransformation"
}
