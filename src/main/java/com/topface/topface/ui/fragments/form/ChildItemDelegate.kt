package com.topface.topface.ui.fragments.form

import com.topface.topface.R
import com.topface.topface.databinding.ChildFormItemBinding

/**
 * Created by tiberal on 01.11.16.
 */
//todo отгородиться от выпадающего наследования

class ChildItemDelegate : ExpandableItemDelegate<ChildFormItemBinding, FormModel>() {

    companion object {
        const val TYPE = 0
    }

    override val bindingClass: Class<ChildFormItemBinding>
        get() = ChildFormItemBinding::class.java
    override val itemLayout: Int
        get() = R.layout.child_form_item

    override fun bind(binding: ChildFormItemBinding, data: ExpandableItem<FormModel>?, position: Int) =
            data?.data?.let { binding.model = ChildItemViewModel(it) }

}