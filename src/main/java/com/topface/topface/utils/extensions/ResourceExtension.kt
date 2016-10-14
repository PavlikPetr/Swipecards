package com.topface.topface.utils.extensions

import android.content.res.Resources
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import com.topface.topface.App
import com.topface.topface.R

/**
 * Created by petrp on 09.10.2016.
 */

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

    @ColorInt
    fun Int.getColor(): Int {
        var res: Int
        try {
            res = App.getContext().resources.getColor(this)
        } catch(e: Resources.NotFoundException) {
            res = android.support.design.R.color.background_material_light
        }
        return res
    }
}
