package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.adapters.BaseHeaderFooterRecyclerViewAdapter
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_utils.getLastItem
import com.topface.topface.ui.fragments.feed.feed_utils.hasItem
import com.topface.topface.utils.Utils

/**
 * Базовый адаптер для всех фидов
 * Created by tiberal on 01.08.16.
 * @param V feed item binding
 * @param T feed item data item
 */
abstract class BaseFeedAdapter<V : ViewDataBinding, T : FeedItem> : BaseHeaderFooterRecyclerViewAdapter<V, T>() {

    var isNeedHighLight: ((T) -> Boolean)? = null
        set(value) {
            isActionModeEnabled = value != null
            field = value
        }
    protected var isActionModeEnabled: Boolean = false

    private fun handleHighlight(binding: V, position: Int) {
        binding.root.isSelected = false
        val item = getDataItem(position)
        if (item?.unread ?: false) {
            binding.root.setBackgroundResource(R.drawable.new_feed_list_item_selector)
        } else {
            binding.root.setBackgroundResource(R.drawable.feed_list_item_selector)
        }
        if (item != null) {
            binding.root.isSelected = isActionModeEnabled && isNeedHighLight?.invoke(item) ?: false
        }
    }

    override fun bindData(binding: V?, position: Int) {
        if (binding != null) {
            handleHighlight(binding, position)
        }
    }

    override fun bindHeader(binding: ViewDataBinding?, position: Int) {
        super.bindHeader(binding, position)
    }

    fun disableAllHighlight() {
        data.forEachIndexed { position, item ->
            item.unread = false
            notifyItemChanged(position)
        }
    }

    override fun getUpdaterEmitObject(): Bundle {
        val last = data.getLastItem()
        val bundle = Bundle()
        bundle.putString(FeedRequestFactory.TO, if (last != null) last.id else Utils.EMPTY)
        return bundle
    }

    fun removeItem(position: Int): Boolean {
        var result = false
        if (data.hasItem(position)) {
            result = true
            data.removeAt(position)
            notifyItemRemoved(position)
        }
        return result
    }

    fun removeItems(items: List<T>): Boolean {
        val result = data.removeAll(items)
        notifyDataSetChanged()
        return result
    }

    override fun getItemId(position: Int): Long {
        return getDataItem(position)?.id?.toLong() ?: super.getItemId(position)
    }

}
