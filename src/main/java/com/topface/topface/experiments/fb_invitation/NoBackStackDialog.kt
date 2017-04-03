package com.topface.topface.experiments.fb_invitation;

import android.app.Dialog
import android.content.Context
import android.support.annotation.StyleRes
import com.topface.topface.R
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.utils.extensions.showShortToast
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Диалог, который закроет приложение по двойному backPressed
 */
class NoBackStackDialog(context: Context, @StyleRes theme: Int, private val mCallFinish: () -> Unit) : Dialog(context, theme) {

    private val mBackPressedOnce = AtomicBoolean(false)
    private var mTimerSubscription: Subscription? = null

    override fun onBackPressed() {
        if (!mBackPressedOnce.get()) {
            mBackPressedOnce.set(true)
            startTimer()
            R.string.press_back_more_to_close_app.showShortToast()
        } else {
            mCallFinish.invoke()
        }
    }

    private fun startTimer() {
        if (mTimerSubscription?.isUnsubscribed ?: true) {
            mTimerSubscription = Observable.timer(NavigationActivity.EXIT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                    .first()
                    .subscribe(shortSubscription {
                        mBackPressedOnce.set(false)
                    })
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mTimerSubscription.safeUnsubscribe()
    }
}