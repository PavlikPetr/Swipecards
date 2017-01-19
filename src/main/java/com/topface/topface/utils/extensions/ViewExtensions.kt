package com.topface.topface.utils.extensions

import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout

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