package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.EditorSwitchBinding
import com.topface.topface.ui.fragments.buy.design.v1.TestPurchaseSwitchItem
import com.topface.topface.ui.fragments.buy.design.v1.view_models.TestPurchaseViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class TestPurchaseSwitchItemComponent : AdapterComponent<EditorSwitchBinding, TestPurchaseSwitchItem>() {
    override val itemLayout = R.layout.editor_switch
    override val bindingClass = EditorSwitchBinding::class.java

    override fun bind(binding: EditorSwitchBinding, data: TestPurchaseSwitchItem?, position: Int) {
        with(binding) {
            (this.root.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.isFullSpan = true
            viewModel = TestPurchaseViewModel(data?.isChecked ?: App.getUserConfig().testPaymentFlag).viewModel
        }
    }
}