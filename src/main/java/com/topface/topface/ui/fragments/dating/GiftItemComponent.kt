package com.topface.topface.ui.fragments.dating

import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.Gift
import com.topface.topface.databinding.FormGiftItemBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class GiftItemComponent : AdapterComponent<FormGiftItemBinding, Gift>() {

    override val itemLayout: Int
        get() = R.layout.form_gift_item
    override val bindingClass: Class<FormGiftItemBinding>
        get() = FormGiftItemBinding::class.java

    override fun bind(binding: FormGiftItemBinding, data: Gift?, position: Int) {
        Debug.error("--------------------${data?.link}------------------------")
        data?.let { binding.setModel(GiftListItemViewModel(it.link)) }
    }
}