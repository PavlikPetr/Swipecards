package com.topface.topface.ui.bonus.components

import android.support.v7.widget.StaggeredGridLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.OfferwallButtonBinding
import com.topface.topface.ui.bonus.OfferwallButton
import com.topface.topface.ui.bonus.models.OfferwallButtonViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Компонент кнопки для загрузки офервола
 * Created by petrp on 02.06.2017.
 */

class OfferwallButtonComponent : AdapterComponent<OfferwallButtonBinding, OfferwallButton>() {
    override val itemLayout: Int
        get() = R.layout.offerwall_button
    override val bindingClass: Class<OfferwallButtonBinding>
        get() = OfferwallButtonBinding::class.java

    override fun bind(binding: OfferwallButtonBinding, data: OfferwallButton?, position: Int) {
        data?.let { binding.viewModel = OfferwallButtonViewModel(it) }
    }
}