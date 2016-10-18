package com.topface.topface.ui.fragments

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.ActionBar
import android.view.View
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.views.toolbar.BackToolbarViewModel
import com.topface.topface.ui.views.toolbar.BaseToolbarViewModel
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.ToolbarSettingsData

/**
 * Created by ppavlik on 14.10.16.
 * Base activity for a whole project, cause it hold a toolbar
 */

abstract class ToolbarActivity<T : ViewDataBinding> : CrashReportActivity(), IToolbarNavigation {

    lateinit var viewBinding: T
    var toolbarBinding: ToolbarBinding? = null
    var toolbarBaseViewModel: BaseToolbarViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView<T>(this@ToolbarActivity, getLayout())
        setContentView(viewBinding.root)
        toolbarBinding = getToolbarBinding(viewBinding)
        viewBinding.setVariable(BR.toolbarViewModel, getToolbarViewModel())
        setSupportActionBar(toolbarBinding?.toolbar)
        // увы, но колбэк будет работать только если установить его после setSupportActionBar
        toolbarBaseViewModel?.init()
    }

    fun setToolBarVisibility(isVisible: Boolean) {
        toolbarBaseViewModel?.let { it.visibility.set(if (isVisible) View.VISIBLE else View.GONE) }
    }

    fun setToolbarSettings(settings: ToolbarSettingsData) {
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
            settings.isOnline?.let {
                toolbarViewModel.setOnline(it)
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

    protected fun getToolbarViewModel(): BaseToolbarViewModel {
        if (toolbarBaseViewModel == null && toolbarBinding != null) {
            toolbarBinding?.let {
                toolbarBaseViewModel = generateToolbarViewModel(it)
            }
        }

        return toolbarBaseViewModel as BaseToolbarViewModel
    }

    abstract fun getToolbarBinding(binding: T): ToolbarBinding

    @LayoutRes
    abstract fun getLayout(): Int

    open protected fun initActionBarOptions(actionBar: ActionBar?) {
        actionBar?.let {
            it.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toolbarBaseViewModel?.release()
    }
}