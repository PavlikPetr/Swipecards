package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import jp.wasabeef.glide.transformations.CropCircleTransformation

class NewCircleCropTransformation(val mContext: Context) : Transformation<Bitmap> {

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int) =
            CropCircleTransformation(mContext).transform(resource, outWidth, outHeight)

    override fun getId() = "NewCircleCropTransformation"
}
