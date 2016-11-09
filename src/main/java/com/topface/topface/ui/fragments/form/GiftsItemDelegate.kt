package com.topface.topface.ui.fragments.form

import android.support.v7.widget.LinearLayoutManager
import com.topface.topface.R
import com.topface.topface.databinding.GiftsFormItemBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi

/**
 * Делегат для итема с подарочками
 * Created by tiberal on 07.11.16.
 */
class GiftsItemDelegate(private val mApi: FeedApi) : ExpandableItemDelegate<GiftsFormItemBinding, GiftsModel>() {

    companion object {
        const val TYPE = 2
    }

    override val itemLayout: Int
        get() = R.layout.gifts_form_item
    override val bindingClass: Class<GiftsFormItemBinding>
        get() = GiftsFormItemBinding::class.java

    override fun bind(binding: GiftsFormItemBinding, data: ExpandableItem<GiftsModel>?, position: Int) {
        binding.giftsList.layoutManager = LinearLayoutManager(binding.root.context.applicationContext
                , LinearLayoutManager.HORIZONTAL, false)
        data?.data?.let { giftsModel ->
            if (giftsModel.gifts != null) {
                val adapter = FormGiftsAdapter()
                adapter.addData(giftsModel.gifts.items)
                val viewModel = GiftsItemViewModel(mApi, giftsModel.gifts, giftsModel.userId) {
                    adapter.addData(it.items)
                    adapter.notifyDataSetChanged()
                }
                adapter.updaterObservable.subscribe {
                    if (giftsModel.gifts.items.isNotEmpty()) {
                        viewModel.loadGifts(adapter.data.count(), adapter.data.last().feedId)
                    }
                }
                binding.giftsList.adapter = adapter
                binding.model = viewModel
            }
        }
    }

}