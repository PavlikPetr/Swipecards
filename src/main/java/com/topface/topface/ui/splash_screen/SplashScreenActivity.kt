package com.topface.topface.ui.splash_screen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.ViewPropertyAnimator
import android.view.Window
import com.topface.topface.R
import com.topface.topface.databinding.AcSplashBinding
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.analytics.TrackedFragmentActivity
import com.topface.topface.utils.extensions.getDimen


class SplashScreenActivity : TrackedFragmentActivity() {

    companion object {
        private const val PASSED_TIME = "passed_time"
        private const val ANIMATION_DURATION = 3000L
    }

    private lateinit var binding: AcSplashBinding
    private var mAnimator: ViewPropertyAnimator? = null
    private val mHandler = Handler()
    private val mRunnable = { splashFinished() }
    private var mStartTime = 0L          // время запуска сплеша
    private var mTimeLeft = 0L          // оставшееся время для работы сплеша
    private var mNeedFinish = false     // флаг, который дает нам понять, что сплеш отработал и можно дестроить активити на onStop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<AcSplashBinding>(this, R.layout.ac_splash)
        mTimeLeft = savedInstanceState?.getLong(PASSED_TIME) ?: ANIMATION_DURATION
    }

    private fun splashFinished() {
        mNeedFinish = true
        startActivity(createMainActivityIntent(NavigationActivity::class.java))
    }

    // оверайдим onBackPressed и не вызываем super, чтобы лишить юзера возможности закрыть сплеш
    override fun onBackPressed() {
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putLong(PASSED_TIME, calculateTimeLeft())
    }

    override fun onResume() {
        super.onResume()
        if (mTimeLeft == ANIMATION_DURATION) {
            binding.logo.post {
                mAnimator = binding.logo.animate().apply {
                    duration = ANIMATION_DURATION
                    translationY(-(binding.logo.top - R.dimen.tf_logo_top.getDimen()) + getStatusBarHeight())
                    setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            splashFinished()
                        }
                    })
                    mStartTime = System.currentTimeMillis()
                }
            }
        } else {
            mHandler.postDelayed(mRunnable, mTimeLeft)
        }
    }

    override fun onPause() {
        super.onPause()
        mTimeLeft = calculateTimeLeft()
        mHandler.removeCallbacks(mRunnable)
        binding.logo.animate().setListener(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mRunnable)
    }

    override fun onStop() {
        super.onStop()
        if (mNeedFinish) {
            finish()
        }
    }

    private fun calculateTimeLeft() = System.currentTimeMillis() - mStartTime

    fun getStatusBarHeight(): Int {
        val rectangle = Rect().apply {
            window.decorView.getWindowVisibleDisplayFrame(this)
        }
        val titleBarHeight = window.findViewById(Window.ID_ANDROID_CONTENT).top - rectangle.top
        return Math.abs(titleBarHeight)
    }

    private fun <T> createMainActivityIntent(clazz: Class<T>) = Intent(applicationContext, clazz)
}