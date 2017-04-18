package com.topface.topface.ui.fragments.dating.form

import com.topface.topface.R
import com.topface.topface.databinding.ParentItemBinding
import com.topface.topface.ui.new_adapter.ExpandableItem
import com.topface.topface.ui.new_adapter.ExpandableItemDelegate

/**
 * Итем для анкеты отображающий группу каких то данных(статус, о себе, город)
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

    override fun bind(binding: ParentItemBinding, data: ExpandableItem<ParentModel>?, position: Int){
//            data?.let { binding.model = ParentItemViewModel(it, position) }
    }

}