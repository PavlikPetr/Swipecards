package com.topface.topface.ui.fragments.buy.pn_purchase.components

import com.topface.topface.R
import com.topface.topface.api.IApi
import com.topface.topface.databinding.EditorSwitchBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.InvisibleModeSwitch
import com.topface.topface.ui.fragments.buy.pn_purchase.InvisibleModeViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для режима невидимки
 * Created by ppavlik on 26.05.17.
 */
class InvisibleModeSwitchComponent(private var mApi: IApi) : AdapterComponent<EditorSwitchBinding, InvisibleModeSwitch>() {
    override val itemLayout: Int
        get() = R.layout.editor_switch
    override val bindingClass: Class<EditorSwitchBinding>
        get() = EditorSwitchBinding::class.java

    private var mViewModel: InvisibleModeViewModel? = null

    override fun bind(binding: EditorSwitchBinding, data: InvisibleModeSwitch?, position: Int) {
        mViewModel = InvisibleModeViewModel(mApi)
        binding.viewModel = mViewModel?.viewModel
    }

    override fun release() {
        mViewModel?.release()
    }
}