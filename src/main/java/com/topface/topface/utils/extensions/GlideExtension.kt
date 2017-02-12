package com.topface.topface.utils.extensions

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target

/**
 * Плюшки для glide
 * Created by petrp on 09.02.2017.
 */

/**
 * Cancel any pending loads Glide may have for the target and free any resources (such as {@link Bitmap}s) that may
 * have been loaded for the target so they may be reused.
 */
fun <T> Target<T>?.clear() = this?.let { Glide.clear(it) }