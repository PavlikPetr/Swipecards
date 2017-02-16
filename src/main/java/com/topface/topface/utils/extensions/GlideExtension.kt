package com.topface.topface.utils.extensions

import android.graphics.Point
import com.bumptech.glide.BitmapTypeRequest
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.StringSignature
import com.topface.topface.App
import com.topface.topface.utils.Utils

/**
 * Плюшки для glide
 * Created by petrp on 09.02.2017.
 */

/**
 * Cancel any pending loads Glide may have for the target and free any resources (such as {@link Bitmap}s) that may
 * have been loaded for the target so they may be reused.
 */
fun <T> Target<T>?.clear() = this?.let { Glide.clear(it) }

fun DrawableRequestBuilder<String>.loadLinkToSameCache(link: String) = apply {
    load(link)
            .signature(StringSignature(link))
            .override(ScreenSize.width, ScreenSize.height)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
}

object ScreenSize {
    private val mScreenSize: Point by lazy {
        Utils.getSrceenSize(App.getContext())
    }

    val height = mScreenSize.y
    val width = mScreenSize.x
}