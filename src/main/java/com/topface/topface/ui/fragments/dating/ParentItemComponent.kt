package com.topface.topface.ui.fragments.dating

import com.topface.topface.R
import com.topface.topface.databinding.ParentItemBinding
import com.topface.topface.ui.fragments.dating.form.ParentItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class ParentItemComponent : AdapterComponent<ParentItemBinding, ParentModel>() {

    override val itemLayout: Int
        get() = R.layout.parent_item

    override val bindingClass: Class<ParentItemBinding>
        get() = ParentItemBinding::class.java

    override fun bind(binding: ParentItemBinding, data: ParentModel?, position: Int) {
        data?.let { binding.model = ParentItemViewModel(it, position) }
    }
}