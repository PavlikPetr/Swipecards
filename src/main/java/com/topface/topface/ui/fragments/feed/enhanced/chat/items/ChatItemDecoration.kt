package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.NO_POSITION
import android.view.View
import com.topface.topface.R
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.api.responses.isFriendItem
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatViewModel
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
        var bottomMargin = 0

        if (view != null && parent != null && state != null) {
            val position = parent.getChildAdapterPosition(view)
            if (position != NO_POSITION) {
                (parent.adapter as? CompositeAdapter)?.data?.let {
                    if (position == 0) bottomMargin = marginBig
                    // dividers text/visible calculation
                    it.filterIsInstance<HistoryItem>().prepareDividers()
                    val itemCount = it.size

                    // show/hide avatar of friend messages
                    // must show avatar only at first message in list
                    // list may be broken by divider
                    (it[position] as? HistoryItem)?.let { currentItem ->
                        topMargin = marginBig
                        if (currentItem.isFriendItem()) {
                            if (currentItem.isDividerVisible.get()) {
                                currentItem.isAvatarVisible.set(true)
                            } else {
                                when (position) {
                                // first item
                                    itemCount - 1 -> {
                                        currentItem.isAvatarVisible.set(true)
                                    }
                                // middle and last items
                                    else -> {
                                        (it[position + 1] as? HistoryItem)?.let { prevItem ->
                                            if (prevItem.isFriendItem()) {
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
                            if (position < itemCount - 1) {
                                (it[position + 1] as? HistoryItem)?.let { prevItem ->
                                    if (!prevItem.isFriendItem() && !currentItem.isDividerVisible.get()) {
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
        outRect?.set(0, topMargin, 0, bottomMargin)
    }
}

fun List<HistoryItem>?.prepareDividers() =
        if (this != null && this.isNotEmpty()) {
            // group HistoryItems in our list by "round" day
            val dividers : MutableMap<Long, MutableList<HistoryItem>> =
                    this.reversed().groupByTo(mutableMapOf()) {
                        it.created - it.created % 86400
                    }
            // for each stored day, mark all items have no divider
            // and enable divider only for last added, also, update divider text to correspond to
            // "today", "yesterday", "day.month" (if current year), "day.month.year" for all other
            for (day in dividers.keys) {
                dividers[day]?.forEach { it.isDividerVisible.set(false) }
                if (dividers[day]?.isNotEmpty() ?: false) {
                    dividers[day]?.first()?.apply {
                        isDividerVisible.set(true)
                        // don't forget about server timestamps, they use seconds, but we millis
                        dividerText.set(DateUtils.getRelativeDate(day * ChatViewModel.SERVER_TIME_CORRECTION))
                    }
                }
            }
        } else Unit