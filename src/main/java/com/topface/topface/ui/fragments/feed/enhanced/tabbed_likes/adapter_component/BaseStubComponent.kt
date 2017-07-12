package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.adapter_component

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.databinding.BaseSympathyStubLayoutBinding
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs.BaseSympathyStubViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 *  Базовый компонент для стабов
 */
abstract class BaseStubComponent<D>() : AdapterComponent<BaseSympathyStubLayoutBinding, D>() {

    abstract val stubTitleText: String
    abstract val stubText: String
    abstract val greenButtonText: String
    abstract val borderlessButtonText: String
    abstract fun greenButtonAction(): Unit
    abstract fun onBorderlessButtonPress(): Unit

    override val itemLayout: Int
        get() = com.topface.topface.R.layout.base_sympathy_stub_layout
    override val bindingClass: Class<BaseSympathyStubLayoutBinding>
        get() = BaseSympathyStubLayoutBinding::class.java

    private var mViewModel: BaseSympathyStubViewModel? = null

    override fun bind(binding: BaseSympathyStubLayoutBinding, data: D?, position: Int) =
            with(binding) {
                    mViewModel = BaseSympathyStubViewModel(stubTitleText, stubText,
                            greenButtonText, borderlessButtonText,
                            { greenButtonAction() }, { onBorderlessButtonPress() })
                viewModel = mViewModel
                root.layoutParams = android.support.v7.widget.StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT).apply { isFullSpan = true }
            }
}