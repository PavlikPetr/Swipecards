package com.topface.topface.ui.fragments.dating

import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.ParentItemBinding
import com.topface.topface.ui.fragments.dating.form.ParentItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class ParentItemComponent : AdapterComponent<ParentItemBinding, ParentModel>() {

    override val itemLayout: Int
        get() = R.layout.parent_item


    init {
        Debug.error("---------------КОНСТРУКТОР Парент Итем Компонент-----------")
    }
    override val bindingClass: Class<ParentItemBinding>
        get() = ParentItemBinding::class.java


    override fun bind(binding: ParentItemBinding, data: ParentModel?, position: Int)
    {
        Debug.error("------------------${data?.data}------------")
        data?.let {ParentItemViewModel(it, position)}
    }
}