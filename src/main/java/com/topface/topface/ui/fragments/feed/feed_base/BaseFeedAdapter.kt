package com.topface.topface.ui.fragments.feed.feed_base

import android.os.Bundle
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_utils.getLastItem
import com.topface.topface.ui.fragments.feed.feed_utils.hasItem
import com.topface.topface.utils.Utils

/**
 * Базовый адаптер для всех фидов
 * Created by tiberal on 01.08.16.
 */
abstract class BaseFeedAdapter<T : FeedItem> : BaseRecyclerViewAdapter<FeedItemHeartBinding, T>() {

    init {
        setHasStableIds(true)
    }

    var isNeedHighLight: ((T) -> Boolean)? = null
        set(value) {
            if (value != null) {
                isActionModeEnabled = true
            } else {
                isActionModeEnabled = false
            }
            field = value
        }
    protected var isActionModeEnabled: Boolean = false

    private fun handleHighlight(binding: FeedItemHeartBinding, position: Int) {
        binding.root.isSelected = false
        if (data[position].unread) {
            binding.root.setBackgroundResource(R.drawable.new_feed_list_item_selector)
        } else {
            binding.root.setBackgroundResource(R.drawable.feed_list_item_selector)
        }
        if (isActionModeEnabled) {
            if (isNeedHighLight?.invoke(data[position]) ?: false) {
                binding.root.isSelected = true
            } else {
                binding.root.isSelected = false
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return data[position].id.toLong()
    }

    override fun bindData(binding: FeedItemHeartBinding?, position: Int) {
        if (binding != null) {
            handleHighlight(binding, position)
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
            notifyDataSetChanged()
        }
        return result
    }

    fun removeItems(items: List<T>): Boolean {
        val result = data.removeAll(items)
        notifyDataSetChanged()
        return result
    }

}
