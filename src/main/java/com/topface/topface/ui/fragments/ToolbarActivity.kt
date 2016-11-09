package com.topface.topface.ui.fragments

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.ActionBar
import android.util.AttributeSet
import android.view.View
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.CrashReportActivity
import com.topface.topface.ui.views.toolbar.view_models.BackToolbarViewModel
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.utils.IToolbarSettings
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData

/**
 * Created by ppavlik on 14.10.16.
 * Base activity for a whole project, cause it hold a toolbar
 */

abstract class ToolbarActivity<T : ViewDataBinding> : CrashReportActivity(), IToolbarNavigation, IToolbarSettings {

    lateinit var viewBinding: T
    var toolbarBinding: ToolbarBinding? = null
    private var mToolbarBaseViewModel: BaseToolbarViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToolbarManager.registerSettingsListener(this)
        viewBinding = DataBindingUtil.setContentView<T>(this, getLayout())
        setContentView(viewBinding.root)
        toolbarBinding = getToolbarBinding(viewBinding)
        viewBinding.setVariable(BR.toolbarViewModel, getToolbarViewModel())
        setSupportActionBar(toolbarBinding?.toolbar)
        initActionBarOptions(supportActionBar)
        // увы, но колбэк будет работать только если установить его после setSupportActionBar
        mToolbarBaseViewModel?.init()
    }

    fun setToolBarVisibility(isVisible: Boolean) {
        mToolbarBaseViewModel?.visibility?.set(if (isVisible) View.VISIBLE else View.GONE)
    }

    open fun setToolbarSettings(settings: ToolbarSettingsData) {
        getToolbarViewModel().let { toolbarViewModel ->
            settings.title?.let {
                toolbarViewModel.title.set(it)
            }
            settings.subtitle?.let {
                toolbarViewModel.subTitle.set(it)
            }
            settings.icon?.let {
                toolbarViewModel.upIcon.set(it)
            }
        }
    }

    override fun onUpButtonClick() {
        onUpClick()
    }

    open fun onUpClick() {
        if (doPreFinish()) {
            if (!onSupportNavigateUp()) {
                finish()
            }
        }
    }

    fun doPreFinish(): Boolean {
        return onPreFinish()
    }

    open protected fun onPreFinish(): Boolean {
        return true
    }

    open protected fun generateToolbarViewModel(toolbar: ToolbarBinding): BaseToolbarViewModel {
        return BackToolbarViewModel(toolbar, getString(R.string.app_name), this)
    }

    fun getToolbarViewModel(): BaseToolbarViewModel {
        if (mToolbarBaseViewModel == null && toolbarBinding != null) {
            toolbarBinding?.let {
                mToolbarBaseViewModel = generateToolbarViewModel(it)
            }
        }

        return mToolbarBaseViewModel as BaseToolbarViewModel
    }

    abstract fun getToolbarBinding(binding: T): ToolbarBinding

    @LayoutRes
    abstract fun getLayout(): Int

    open protected fun initActionBarOptions(actionBar: ActionBar?) {
        actionBar?.setDisplayShowHomeEnabled(true)
    }


    override fun onToolbarSettings(settings: ToolbarSettingsData) {
        setToolbarSettings(settings)
    }

    override fun onDestroy() {
        super.onDestroy()
        ToolbarManager.unregisterSettingsListener(this)
        mToolbarBaseViewModel?.release()
    }
}