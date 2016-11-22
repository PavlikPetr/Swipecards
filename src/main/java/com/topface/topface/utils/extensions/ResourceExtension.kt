package com.topface.topface.utils.extensions

import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import com.topface.topface.App
import com.topface.topface.R
import java.util.*

/**
 * Created by petrp on 09.10.2016.
 */

@JvmOverloads
fun Int.getString(default: String = ""): String {
    var res: String
    try {
        res = App.getContext().getString(this)
    } catch(e: Resources.NotFoundException) {
        res = default
    }
    return res
}

fun Int.getString(default: Int): String {
    return this.getString(default.getString())
}

@ColorRes
@JvmOverloads
fun Int.getColor(default: Int = Color.BLACK): Int {
    var res: Int
    try {
        res = App.getContext().resources.getColor(this)
    } catch(e: Resources.NotFoundException) {
        res = default
    }
    return res
}

@JvmOverloads
@DimenRes
fun Int.getDimen(default: Float = 0f): Float {
    var res: Float
    try {
        res = App.getContext().resources.getDimension(this)
    } catch(e: Resources.NotFoundException) {
        res = default
    }
    return res
}

@DrawableRes
fun Int.getDrawable(): Drawable? {
    var res: Drawable?
    try {
        val resources = App.getContext().resources
        res = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            resources.getDrawable(this, null)
        else
            resources.getDrawable(this)
    } catch(e: Resources.NotFoundException) {
        res = null
    }
    return res
}

fun Int.isHasNotification(): Boolean {
    return when (this) {
        R.drawable.menu_white_notification, R.drawable.menu_gray_notification -> true
        else -> false
    }
}

/**
 * The method to convert an array of resources to List<Int>.
 * method's parameter is defaultValue(Int) of ID resource.
 * Method creates an array(TypedArray) for ID of array Resource and convert to List<Int>
 */

fun Int.fromObtainToInt(@DrawableRes defRes: Int): List<Int> {
    val arr: TypedArray
    try {
        arr = App.getContext().resources.obtainTypedArray(this)
    } catch(e: Resources.NotFoundException) {
        return listOf()
    }
    val usersFakeArray = ArrayList<Int>()
    for (i in 0..arr.length() - 1) {
        usersFakeArray.add(arr.getResourceId(i, defRes))
    }
    arr.recycle()
    return usersFakeArray
}
