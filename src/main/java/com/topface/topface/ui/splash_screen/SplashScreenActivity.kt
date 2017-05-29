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
    private val mRunnable = { startActivity(createMainActivityIntent(NavigationActivity::class.java)) }
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<AcSplashBinding>(this, R.layout.ac_splash)
        if (savedInstanceState == null) {
            binding.logo.post {
                binding.logo.alpha = 0f
                mAnimator = binding.logo.animate().apply {
                    duration = ANIMATION_DURATION
                    alpha(1f)
                    translationY(-(binding.logo.top - R.dimen.tf_logo_top.getDimen()) + getStatusBarHeight())
                    setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            startActivity(createMainActivityIntent(NavigationActivity::class.java))
                        }
                    })
                    startTime = System.currentTimeMillis()
                }
            }
        } else {
            mHandler.postDelayed(mRunnable, savedInstanceState.getLong(PASSED_TIME))
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putLong(PASSED_TIME, System.currentTimeMillis() - startTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mRunnable)
    }

    fun getStatusBarHeight(): Int {
        val rectangle = Rect().apply {
            window.decorView.getWindowVisibleDisplayFrame(this)
        }
        val titleBarHeight = window.findViewById(Window.ID_ANDROID_CONTENT).top - rectangle.top
        return Math.abs(titleBarHeight)
    }

    private fun <T> createMainActivityIntent(clazz: Class<T>) = Intent(applicationContext, clazz)
}