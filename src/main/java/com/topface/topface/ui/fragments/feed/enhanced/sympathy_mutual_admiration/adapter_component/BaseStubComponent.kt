package com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.adapter_component

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.BaseSympathyStubLayoutBinding
import com.topface.topface.ui.fragments.feed.enhanced.sympathy_mutual_admiration.stubs.BaseSympathyStubViewModel
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent
import com.topface.topface.utils.extensions.getString

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

    override val itemLayout = com.topface.topface.R.layout.base_sympathy_stub_layout
    override val bindingClass = BaseSympathyStubLayoutBinding::class.java

    override fun bind(binding: BaseSympathyStubLayoutBinding, data: D?, position: Int) =
            with(binding) {
                viewModel = BaseSympathyStubViewModel(stubTitleText, stubText,
                        greenButtonText, borderlessButtonText,
                        { greenButtonAction() }, { onBorderlessButtonPress() })
                root.layoutParams = android.support.v7.widget.StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                        StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT).apply { isFullSpan = true }
            }
}