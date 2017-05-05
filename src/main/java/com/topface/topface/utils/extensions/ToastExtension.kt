package com.topface.topface.utils.extensions

import android.annotation.SuppressLint
import android.support.annotation.IntDef
import android.view.InflateException
import android.widget.Toast
import com.topface.topface.App

/**
 * Extension для рпоказа toast
 * Created by ppavlik on 17.01.17.
 */
/** @hide
 */
@IntDef(DurationConst.LENGTH_SHORT, DurationConst.LENGTH_LONG)
@Retention(AnnotationRetention.SOURCE)
annotation class Duration

object DurationConst {
    /**
     * Show the view or text notification for a short period of time.  This time
     * could be user-definable.  This is the default.
     * @see .setDuration
     */
    const val LENGTH_SHORT = 0L

    /**
     * Show the view or text notification for a long period of time.  This time
     * could be user-definable.
     * @see .setDuration
     */
    const val LENGTH_LONG = 1L
}

fun String.showShortToast() = showToast(DurationConst.LENGTH_SHORT)

fun String.showLongToast() = showToast(DurationConst.LENGTH_LONG)

fun Int.showLongToast() = getString().showToast(DurationConst.LENGTH_LONG)

fun Int.showShortToast() = getString().showToast(DurationConst.LENGTH_SHORT)

@SuppressLint("ShowToast")
fun String.showToast(@Duration duration: Long) {
    val context = App.getContext()
    if (this.isNotEmpty() && context != null) {
        val toast: Toast?
        try {
            /*
                краш при инфлейте тоста на соньках
                https://rink.hockeyapp.net/manage/apps/26531/app_versions/343/crash_reasons/110273859?scope=devices&type=statistics
                 */
            toast = Toast.makeText(
                    context,
                    this,
                    duration.toInt()
            )
        } catch (e: InflateException) {
            e.printStackTrace()
            return
        }
        toast?.show()
    }
}