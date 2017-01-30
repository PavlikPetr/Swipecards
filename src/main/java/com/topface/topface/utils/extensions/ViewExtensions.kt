package com.topface.topface.utils.extensions

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.topface.framework.imageloader.BitmapUtils
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.utils.glide_utils.BlurTransformation
import com.topface.topface.utils.glide_utils.GlideTransformationFactory
import com.topface.topface.utils.glide_utils.GlideTransformationType
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.backgroundDrawable
import rx.Observable
import rx.Subscription

/**
 * Упрощалуи для работы с виьюхами
 * Created by tiberal on 27.07.16.
 */

fun EditText.getStringText() = this.text.toString()

@JvmOverloads
fun View.setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) =
        when (layoutParams.javaClass) {
            RelativeLayout.LayoutParams::class.java -> (layoutParams as? RelativeLayout.LayoutParams)
                    ?.let { it.setMargins(left ?: it.leftMargin, top ?: it.topMargin, right ?: it.rightMargin, bottom ?: it.bottomMargin) }
            LinearLayout.LayoutParams::class.java -> (layoutParams as? LinearLayout.LayoutParams)
                    ?.let { it.setMargins(left ?: it.leftMargin, top ?: it.topMargin, right ?: it.rightMargin, bottom ?: it.bottomMargin) }
            FrameLayout.LayoutParams::class.java -> (layoutParams as? FrameLayout.LayoutParams)
                    ?.let { it.setMargins(left ?: it.leftMargin, top ?: it.topMargin, right ?: it.rightMargin, bottom ?: it.bottomMargin) }
            else -> Unit
        }

@JvmOverloads
fun View.setPadding(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) =
        setPadding(left ?: paddingLeft, top ?: paddingTop, right ?: paddingRight, bottom ?: paddingBottom)

fun View.loadBackground(link: String): Observable<BitmapDrawable> {
    return Observable.create<BitmapDrawable> {
        it.onNext(with(Glide.with(context)
                .load(link)
                .asBitmap()
                .transform(BlurTransformation(context, 10))
                .placeholder(R.drawable.bg_blur)
//                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(getMeasuredWidth(), getMeasuredHeight())
                .get()) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                BitmapDrawable(this@loadBackground.getContext().getResources(), this)
            } else {
                BitmapDrawable(this)
            }
        })
    }
}