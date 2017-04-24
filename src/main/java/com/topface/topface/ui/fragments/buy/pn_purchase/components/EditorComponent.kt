package com.topface.topface.ui.fragments.buy.pn_purchase.components

import com.topface.topface.R
import com.topface.topface.databinding.EditorSwitchBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.EditorSwitch
import com.topface.topface.ui.fragments.buy.pn_purchase.EditorViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для переключателя тестовых покупок
 * Created by ppavlik on 24.04.17.
 */
class EditorComponent : AdapterComponent<EditorSwitchBinding, EditorSwitch>() {
    override val itemLayout: Int
        get() = R.layout.editor_switch
    override val bindingClass: Class<EditorSwitchBinding>
        get() = EditorSwitchBinding::class.java

    private var mViewModel: EditorViewModel? = null

    override fun bind(binding: EditorSwitchBinding, data: EditorSwitch?, position: Int) {
        mViewModel = EditorViewModel(data?.isChecked ?: false)
        binding.viewModel = mViewModel?.viewModel
    }

    override fun release() {
        mViewModel?.release()
    }
}