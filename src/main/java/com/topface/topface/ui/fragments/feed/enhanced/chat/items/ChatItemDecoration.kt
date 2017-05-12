package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.NO_POSITION
import android.view.View
import com.topface.topface.R
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.getDimen

/**
 * Item decoration for chat items
 */
class ChatItemDecoration : RecyclerView.ItemDecoration() {
    private val marginBig = R.dimen.chat_d1_item_top_margin_big.getDimen().toInt()
    private val marginSmall = R.dimen.chat_d1_item_top_margin_small.getDimen().toInt()

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {

        var topMargin = 0
        fun isFriendItem(item: HistoryItem) = item.getItemType() == HistoryItem.FRIEND_MESSAGE || item.getItemType() == HistoryItem.FRIEND_GIFT

        if (view != null && parent != null && state != null) {
            val position = parent.getChildAdapterPosition(view)
            if (position != NO_POSITION) {
                (parent.adapter as? CompositeAdapter)?.data?.let {
                    // dividers text/visible calculation
                    prepareDividers(it.filterIsInstance<HistoryItem>())

                    // show/hide avatar of friend messages
                    // must show avatar only at first message in list
                    // list may be broken by divider
                    (it[position] as? HistoryItem)?.let { currentItem ->
                        topMargin = marginBig
                        if (isFriendItem(currentItem)) {
                            if (currentItem.isDividerVisible.get()) {
                                currentItem.isAvatarVisible.set(true)
                            } else {
                                when (position) {
                                // first item
                                    0 -> {
                                        currentItem.isAvatarVisible.set(true)
                                    }
                                // middle and last items
                                    else -> {
                                        (it[position - 1] as? HistoryItem)?.let { prevItem ->
                                            if (isFriendItem(prevItem)) {
                                                topMargin = marginSmall
                                                currentItem.isAvatarVisible.set(false)
                                            } else {
                                                currentItem.isAvatarVisible.set(true)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (position > 0) {
                                (it[position - 1] as? HistoryItem)?.let { prevItem ->
                                    if (!isFriendItem(prevItem) && !currentItem.isDividerVisible.get()) {
                                        // small top divider only if prev item is not from friend
                                        // and current item does not have divider enabled
                                        topMargin = marginSmall
                                    }
                                }
                            } else {
                            }
                        }
                    }
                }
            }
        }
        outRect?.set(0, topMargin, 0, 0)
    }

    private fun prepareDividers(items: List<HistoryItem>) {
        if (items.isNotEmpty()) {
            // group HistoryItems in our list by "round" day
            val dividers : MutableMap<Long, MutableList<HistoryItem>> =
                    items.reversed().groupByTo(mutableMapOf()) {
                        it.created - it.created % 86400
                    }
            // for each stored day, mark all items have no divider
            // and enable divider only for last added, also, update divider text to correspond to
            // "today", "yesterday", "day.month" (if current year), "day.month.year" for all other
            for (day in dividers.keys) {
                dividers[day]?.forEach { it.isDividerVisible.set(false) }
                dividers[day]?.last()?.apply {
                    isDividerVisible.set(true)
                    // don't forget about server timestamps, they use seconds, but we millis
                    dividerText.set(DateUtils.getRelativeDate(day * 1000L))
                }
            }
        }
    }
}