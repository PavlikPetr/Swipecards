package com.topface.billing.ninja

import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinjaAddCardBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.BaseFragmentActivity
import android.view.View

/**
 * Add bank card activity
 * Created by m.bayutin on 02.03.17.
 */
class NinjaAddCardActivity: BaseFragmentActivity<LayoutNinjaAddCardBinding>() {
    override fun getToolbarBinding(binding: LayoutNinjaAddCardBinding): ToolbarBinding {
        // дада, отдаем биндинг и скрываем вьюху, ибо этот метод _надо_ реализовать, so sad
        binding.toolbarInclude.root?.visibility = View.GONE
        return binding.toolbarInclude
    }

    override fun getLayout(): Int = R.layout.layout_ninja_add_card
}