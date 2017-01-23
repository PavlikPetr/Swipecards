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
import com.topface.framework.imageloader.BitmapUtils
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.backgroundDrawable
import rx.Observable

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

fun View.loadBackground(link: String) =
        Observable.create<BitmapDrawable> {
            it.onNext(with(BitmapUtils.fastBlur(Glide.with(getContext())
                    .load(link)
                    .asBitmap()
                    .centerCrop()
                    .animate(R.anim.background_animation)
                    .into(getMeasuredWidth(), getMeasuredHeight())
                    .get(), 10)) {
                Debug.error("LOAD_BACKGROUND catch bitmap $this size ${this.height} X ${this.width}")
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Debug.error("LOAD_BACKGROUND api>=16 try to create drawableBitmap")
                    BitmapDrawable(this@loadBackground.getContext().getResources(), this).run {
                        Debug.error("LOAD_BACKGROUND here DrawableBitmap $this with size ${this.intrinsicHeight} X ${this.intrinsicWidth}")
                        this
                    }
                } else {
                    BitmapDrawable(this)
                }
            })
        }
                .applySchedulers()
                .subscribe({
                    Debug.error("LOAD_BACKGROUND catch BitmapDrawable $it with size ${it.intrinsicHeight} X ${it.intrinsicWidth}")
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Debug.error("LOAD_BACKGROUND api>=16 set DrawableBitmap to background")
                        background = it
                    } else {
                        backgroundDrawable = it
                    }
                }, {
                    Debug.error("LOAD_BACKGROUND catch error ${it.message}")
                }, {
                    Debug.error("LOAD_BACKGROUND catch complete")
                })