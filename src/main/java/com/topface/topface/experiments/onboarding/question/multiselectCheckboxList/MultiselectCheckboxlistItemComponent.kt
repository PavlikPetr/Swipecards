package com.topface.topface.experiments.onboarding.question.multiselectCheckboxList

import com.topface.topface.R
import com.topface.topface.databinding.MultiselectCheckboxListItemBinding
import com.topface.topface.experiments.onboarding.question.MultiselectListItem
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class MultiselectCheckboxlistItemComponent: AdapterComponent<MultiselectCheckboxListItemBinding, MultiselectListItem>() {

    override val itemLayout =  R.layout.multiselect_checkbox_list_item

    override val bindingClass =  MultiselectCheckboxListItemBinding::class.java

    override fun bind(binding:MultiselectCheckboxListItemBinding, data: MultiselectListItem?, position: Int) {
        data?.let {
            binding.viewModel = MultiSelectCheckboxItemViewModel(data)
            }
    }

}