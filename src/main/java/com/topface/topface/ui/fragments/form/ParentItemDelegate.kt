package com.topface.topface.ui.fragments.form

import com.topface.topface.R
import com.topface.topface.databinding.ParentItemBinding

/**
 * Created by tiberal on 02.11.16.
 */
class ParentItemDelegate : ExpandableItemDelegate<ParentItemBinding, ParentModel>() {

    companion object {
        const val TYPE = 1
    }

    override val itemLayout: Int
        get() = R.layout.parent_item
    override val bindingClass: Class<ParentItemBinding>
        get() = ParentItemBinding::class.java

    override fun bind(binding: ParentItemBinding, data: ExpandableItem<ParentModel>?, position: Int) =
            data?.let { binding.model = ParentItemViewModel(it, expandableList, position) }

}