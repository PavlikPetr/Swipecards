package com.topface.topface.ui.fragments.dating.form

import android.databinding.ViewDataBinding
import com.topface.topface.R
import com.topface.topface.data.Gift
import com.topface.topface.databinding.FormGiftItemBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter

/**
 * Адаптер для подарочков в итеме анкеты
 * Created by tiberal on 07.11.16.
 */
class FormGiftsAdapter(var hasGifts: Boolean) : BaseRecyclerViewAdapter<ViewDataBinding, Gift>() {

    init {
        if (!hasGifts) {
            addFirst(Gift.constructFakeGift())
        }
    }

    override fun getUpdaterEmitObject() = null

    override fun getItemLayout() = getLayoutId()

    override fun getItemViewType(position: Int) = getLayoutId()

    override fun bindData(binding: ViewDataBinding?, position: Int) {
        binding?.let {
            if (hasGifts) {
                //sorry
                with((it as FormGiftItemBinding).formGiftItem) {
                    setRemoteSrc(data[position].link)
                }
            }
        }
    }

    fun getLayoutId() = if (hasGifts)
        R.layout.form_gift_item
    else
        R.layout.no_gifts_layout
}