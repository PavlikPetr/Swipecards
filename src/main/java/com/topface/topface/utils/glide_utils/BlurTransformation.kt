package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.topface.framework.imageloader.BitmapUtils
import com.topface.topface.R

/**
 * Этот transformation может рисовать восхищение и значок online на аватарке
 * Created by siberia87 on 30.11.16.
 */
class BlurTransformation(mContext: Context, private val radius: Int) : BaseGlideTransformation(mContext) {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        return BitmapResource.obtain(BitmapUtils.fastBlur(resource.get(), radius), mBitmapPool)
    }

    override fun getId() = "BlurTransformation"
}