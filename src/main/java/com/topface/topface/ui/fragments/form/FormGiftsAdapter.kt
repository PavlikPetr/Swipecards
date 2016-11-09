package com.topface.topface.ui.fragments.form

import com.bumptech.glide.Glide
import com.topface.topface.R
import com.topface.topface.data.Gift
import com.topface.topface.databinding.FormGiftItemBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter

/**
 * Адаптер для подарочков в итеме анкеты
 * Created by tiberal on 07.11.16.
 */
class FormGiftsAdapter : BaseRecyclerViewAdapter<FormGiftItemBinding, Gift>() {

    override fun getUpdaterEmitObject() = null

    override fun getItemLayout() = R.layout.form_gift_item

    override fun bindData(binding: FormGiftItemBinding?, position: Int) {
        binding?.formGiftItem?.let {
            it.tag = null
            Glide.with(it.context.applicationContext).load(data[position].link).into(it)
        }
    }
}