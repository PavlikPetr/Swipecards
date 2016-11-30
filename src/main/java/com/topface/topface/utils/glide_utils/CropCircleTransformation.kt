package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*

import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource

/**
 * Этот transformation может скруглять аватарку
 * Created by siberia87 on 25.11.16.
 */
class CropCircleTransformation(mContext: Context) : BaseGlideTransformation(mContext) {
    override fun getId() = "CropCircleTransformation"
}
