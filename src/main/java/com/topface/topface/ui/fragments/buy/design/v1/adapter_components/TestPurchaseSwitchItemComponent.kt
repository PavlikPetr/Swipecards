package com.topface.topface.ui.fragments.buy.design.v1.adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.EditorSwitchBinding
import com.topface.topface.ui.edit.EditSwitcherViewModel
import com.topface.topface.ui.fragments.buy.design.v1.TestPurchaseSwitchItem
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class TestPurchaseSwitchItemComponent: AdapterComponent<EditorSwitchBinding, TestPurchaseSwitchItem>() {
    override val itemLayout = R.layout.editor_switch
    override val bindingClass = EditorSwitchBinding::class.java

    override fun bind(binding: EditorSwitchBinding, data: TestPurchaseSwitchItem?, position: Int) {
        with(binding) {
            viewModel = EditSwitcherViewModel(isCheckedDefault = false, textDefault = "Тестовая покупка")
        }
    }
}