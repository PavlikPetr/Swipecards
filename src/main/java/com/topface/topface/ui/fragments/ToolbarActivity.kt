package com.topface.topface.ui.fragments

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.ActionBar
import android.view.View
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.databinding.ToolbarViewBinding
import com.topface.topface.ui.CrashReportActivity
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.utils.IToolbarSettings
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.ui.views.toolbar.view_models.BackToolbarViewModel
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel

/**
 * Created by ppavlik on 14.10.16.
 * Base activity for a whole project, cause it hold a toolbar_view
 */

abstract class ToolbarActivity<T : ViewDataBinding> : IronSrcIntegrationActivity(), IToolbarNavigation, IToolbarSettings {

    lateinit var viewBinding: T
    var toolbarBinding: ToolbarViewBinding? = null
    private var mToolbarBaseViewModel: BaseToolbarViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView<T>(this, getLayout())
        toolbarBinding = getToolbarBinding(viewBinding)
        viewBinding.setVariable(BR.toolbarViewModel, getToolbarViewModel())
        setSupportActionBar(toolbarBinding?.toolbar)
        initActionBarOptions(supportActionBar)
        // увы, но колбэк будет работать только если установить его после setSupportActionBar
        mToolbarBaseViewModel?.init()
    }

    override fun onResume() {
        super.onResume()
        ToolbarManager.registerSettingsListener(this)
    }

    fun setToolBarVisibility(isToolbarVisible: Boolean) {
        mToolbarBaseViewModel?.rootViewVisibility?.set(if (isToolbarVisible) View.VISIBLE else View.GONE)
    }

    fun isToolBarVisible() = toolbarBinding?.root?.visibility == View.VISIBLE

    open fun setToolbarSettings(settings: ToolbarSettingsData) {
        with(getToolbarViewModel()) {
            settings.title?.let {
                this.title.set(it)
            }
            settings.subtitle?.let {
                this.subTitle.set(it)
            }
            settings.icon?.let {
                this.upIcon.set(it)
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

    open protected fun generateToolbarViewModel(toolbar: ToolbarViewBinding): BaseToolbarViewModel {
        return BackToolbarViewModel(toolbar, getString(R.string.app_name), this)
    }

    fun getToolbarViewModel(): BaseToolbarViewModel {
        if (mToolbarBaseViewModel == null) {
            toolbarBinding?.let {
                mToolbarBaseViewModel = generateToolbarViewModel(it)
            }
        }

        return mToolbarBaseViewModel as BaseToolbarViewModel
    }

    abstract fun getToolbarBinding(binding: T): ToolbarViewBinding

    @LayoutRes
    abstract fun getLayout(): Int

    open protected fun initActionBarOptions(actionBar: ActionBar?) {
        actionBar?.setDisplayShowHomeEnabled(true)
    }


    override fun onToolbarSettings(settings: ToolbarSettingsData) {
        setToolbarSettings(settings)
    }

    override fun onPause() {
        super.onPause()
        ToolbarManager.unregisterSettingsListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mToolbarBaseViewModel?.release()
    }
}