package com.topface.topface.ui.splash_screen

import android.animation.ObjectAnimator
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Rect
import android.os.Bundle
import android.view.Window
import com.topface.topface.R
import com.topface.topface.databinding.AcSplashBinding
import com.topface.topface.ui.analytics.TrackedFragmentActivity
import com.topface.topface.utils.extensions.getDimen


class SplashScreenActivity : TrackedFragmentActivity() {

    lateinit var binding: AcSplashBinding

    lateinit var animator: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<AcSplashBinding>(this, R.layout.ac_splash)
        if (savedInstanceState == null) {
            binding.logo.post {

                animator = ObjectAnimator.ofFloat(binding.logo, "translationY", binding.logo.top.toFloat(),
                        100f + getStatusBarHeight()).apply {
                    duration = 5000
                    start()
                }

                /*binding.logo.alpha = 0f
                with(binding.logo.animate()) {
                    duration = 2000
                    alpha(1f)
                    translationY(-(binding.logo.top - R.dimen.tf_logo_top.getDimen()) + getStatusBarHeight())
                    setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            startActivity(createMainActivityIntent(NavigationActivity::class.java))
                        }
                    })
                }*/
            }
        } else {

            //-(binding.logo.top - R.dimen.tf_logo_top.getDimen())

            animator = ObjectAnimator.ofFloat(binding.logo, "translationY", binding.logo.top.toFloat(),
                    100f + getStatusBarHeight()).apply {
                duration = 5000
                currentPlayTime = 2500
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun getStatusBarHeight(): Int {
        val rectangle = Rect().apply {
            window.decorView.getWindowVisibleDisplayFrame(this)
        }
        val titleBarHeight = window.findViewById(Window.ID_ANDROID_CONTENT).top - rectangle.top
        return Math.abs(titleBarHeight)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun <T> createMainActivityIntent(clazz: Class<T>) = Intent(applicationContext, clazz)
}