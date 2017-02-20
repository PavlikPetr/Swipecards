package com.topface.topface.glide.tranformation

import android.content.Context
import android.graphics.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import jp.wasabeef.glide.transformations.CropCircleTransformation

/**
 * Трансформация круглая и кропнутая для того, чтобы накладыванием сверху заниматься
 */
abstract class CropAndCircleTransformation(context: Context) : Transformation<Bitmap> {

    protected val mContext: Context

    init {
        mContext = context.applicationContext
    }

    protected lateinit var mRemoteBitmap: Bitmap
    protected val mBitmapPool: BitmapPool by lazy {
        Glide.get(mContext).bitmapPool
    }
    internal val mMainBitmap by lazy {
        val minSize = Math.min(mRemoteBitmap.width, mRemoteBitmap.height)
        mBitmapPool.get(minSize, minSize, Bitmap.Config.ARGB_8888) ?: Bitmap.createBitmap(minSize, minSize, Bitmap.Config.ARGB_8888)
    }
    internal val mCanvas by lazy {
        Canvas(mMainBitmap)
    }

    override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        mRemoteBitmap = CropCircleTransformation(mContext).transform(resource, outWidth, outHeight).get()
        val coordinates = ((mRemoteBitmap.width - Math.min(mRemoteBitmap.width, mRemoteBitmap.height)) / 2).toFloat()
        mCanvas.drawBitmap(mRemoteBitmap, coordinates, coordinates, null)
        mRemoteBitmap.recycle()
        return BitmapResource.obtain(mMainBitmap, mBitmapPool)
    }

}
