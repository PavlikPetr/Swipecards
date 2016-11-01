package com.topface.topface.ui.fragments.feed.toolbar

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.topface.topface.R
import com.topface.topface.databinding.AppBarBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.dating.view_etc.DatingButtonsBehavior
import com.topface.topface.ui.views.toolbar.PrimalCollapseViewModel
import org.jetbrains.anko.layoutInflater

/**
 * Базовый фрагмент для фрагментов с collapsing toolbar
 * @param T anchor binding class
 * @param V collapse binding class
 * Created by tiberal on 10.10.16.
 */
abstract class PrimalCollapseFragment<out T : ViewDataBinding, out V : ViewDataBinding> : BaseFragment() {

    abstract val anchorViewResId: Int
    abstract val collapseViewResId: Int
    abstract val toolbarSize: Int
    protected val mAnchorBinding: T by lazy {
        DataBindingUtil.inflate<T>(context.layoutInflater, anchorViewResId, null, false)
    }
    protected val mCollapseBinding: V by lazy {
        DataBindingUtil.inflate<V>(context.layoutInflater, collapseViewResId, null, false)
    }

    private val mAppBarBinding by lazy {
        DataBindingUtil.bind<ToolbarBinding>(activity.findViewById(R.id.toolbar))
    }

    private val mAppBarModel by lazy {
        com.topface.topface.ui.views.toolbar.PrimalCollapseViewModel(mAppBarBinding)
    }

    protected open fun setupToolbar(size: Int) {
        (mAppBarBinding.appbar.layoutParams as CoordinatorLayout.LayoutParams).height = size
    }

    open fun addAnchorViewBehavior() {
        (mAppBarBinding.anchorFrame.layoutParams as CoordinatorLayout.LayoutParams).behavior =
                DatingButtonsBehavior<FrameLayout>()
    }

    open fun bindModels() {
        mAppBarBinding.viewModel = mAppBarModel
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bindModels()
        setupToolbar(toolbarSize)
        addAnchorViewBehavior()
        with(mAppBarBinding) {
            anchorFrame.addView(mAnchorBinding.root)
            collapseFrame.addView(mCollapseBinding.root)
            appbar.addOnOffsetChangedListener(mAppBarModel)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        with(TypedValue()) {
            //устанавливаем стандартный размер тулбара
            if (activity.theme.resolveAttribute(android.R.attr.actionBarSize, this, true)) {
                setupToolbar(TypedValue.complexToDimensionPixelSize(data, resources.displayMetrics))
            }
        }
        with(mAppBarBinding) {
            anchorFrame.removeView(mAnchorBinding.root)
            collapseFrame.removeView(mCollapseBinding.root)
            appbar.removeOnOffsetChangedListener(mAppBarModel)
            appbar.setExpanded(true)
        }
        mAppBarModel.release()
    }
}