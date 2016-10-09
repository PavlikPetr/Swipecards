package com.topface.topface.utils.extensions

import android.content.res.Resources
import com.topface.topface.App

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
}
