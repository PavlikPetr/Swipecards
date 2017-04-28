package com.topface.topface.ui.fragments.buy.pn_purchase.components

import com.topface.topface.R
import com.topface.topface.databinding.EditorSwitchBinding
import com.topface.topface.ui.fragments.buy.pn_purchase.Editor3DSecureSwitchViewModel
import com.topface.topface.ui.fragments.buy.pn_purchase.ThreeDSecurePurchaseSwitch
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент для переключателя тестовых покупок
 * Created by ppavlik on 24.04.17.
 */
class ThreeDSecureComponent : AdapterComponent<EditorSwitchBinding, ThreeDSecurePurchaseSwitch>() {
    override val itemLayout: Int
        get() = R.layout.editor_switch
    override val bindingClass: Class<EditorSwitchBinding>
        get() = EditorSwitchBinding::class.java

    private var mViewModel: Editor3DSecureSwitchViewModel? = null

    override fun bind(binding: EditorSwitchBinding, data: ThreeDSecurePurchaseSwitch?, position: Int) {
        mViewModel = Editor3DSecureSwitchViewModel(data?.isChecked ?: false).apply {
            binding.viewModel = viewModel
        }
    }

    override fun release() {
        mViewModel?.release()
    }
}