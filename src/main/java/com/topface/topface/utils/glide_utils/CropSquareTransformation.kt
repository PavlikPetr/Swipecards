package com.topface.topface.utils.glide_utils

import android.content.Context
import android.graphics.*
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource

/** Трансформация кропает в квадрат
 * Created by mbulgakov on 16.01.17.
 */

class CropSquareTransformation(mContext: Context) : BaseGlideTransformation(mContext) {

    override fun getId() = "CropSquareTransformation"

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val inBitmap = resource.get()
        val width = inBitmap.width
        val height = inBitmap.height
        val outBitmap =
                if (width >= height)
                    Bitmap.createBitmap(
                            inBitmap,
                            width / 2 - height / 2,
                            0,
                            height,
                            height
                    )
                else
                    Bitmap.createBitmap(
                            inBitmap,
                            0,
                            height / 2 - width / 2,
                            width,
                            width
                    )
        inBitmap.recycle()

        return BitmapResource.obtain(outBitmap, mBitmapPool)
    }

}