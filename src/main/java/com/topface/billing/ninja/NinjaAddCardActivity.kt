package com.topface.billing.ninja

import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinjaAddCardBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.BaseFragmentActivity
import com.topface.topface.ui.views.toolbar.view_models.EmptyToolbarViewModel

/**
 * Add bank card activity
 * Created by m.bayutin on 02.03.17.
 */
class NinjaAddCardActivity: BaseFragmentActivity<LayoutNinjaAddCardBinding>() {
    override fun getToolbarBinding(binding: LayoutNinjaAddCardBinding) = binding.toolbarInclude

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) = EmptyToolbarViewModel(toolbar)

    override fun getLayout(): Int = R.layout.layout_ninja_add_card
}