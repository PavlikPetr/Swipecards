package com.topface.topface.ui.fragments.dating.form

import android.databinding.ViewDataBinding
import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.data.Gift
import com.topface.topface.databinding.FormGiftItemBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.utils.BindingsAdapters
import com.topface.topface.utils.extensions.getGiftTag
import com.topface.topface.utils.extensions.setUiTestTag

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

    override fun getUpdaterEmitObject() = Bundle().apply {
        putInt(BaseFeedFragmentViewModel.TO, if (data.isNotEmpty()) data.last().feedId else -1)
    }

    override fun getItemLayout() = getLayoutId()

    override fun getItemViewType(position: Int) = getLayoutId()

    override fun bindData(binding: ViewDataBinding?, position: Int) {
        binding?.let {
            //sorry
            val itemBinding = it
            if (hasGifts && itemBinding is FormGiftItemBinding) {
                with(itemBinding.formGiftItem) {
                    val item = data[position]
//                    setRemoteSrc(item.link)
                    BindingsAdapters.setImgeByGlideWithPlaceholder(this, item.link, R.drawable.girl_2)
                    setUiTestTag(item.getGiftTag())
                }
            }
        }
    }

    fun getLayoutId() = if (hasGifts)
        R.layout.form_gift_item
    else
        R.layout.no_gifts_layout
}