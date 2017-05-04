package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

/**
 * Item decoration for chat items
 */
class ChatItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {

        fun isFriendItem(item: HistoryItem) = item.getItemType() == HistoryItem.FRIEND_MESSAGE || item.getItemType() == HistoryItem.FRIEND_GIFT

        if (view != null && parent != null && state != null) {
            val position = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
            (parent.adapter as? CompositeAdapter)?.data?.let {
                // show/hide avatar of friend messages
                // must show avatar only at first message in list
                (it[position] as? HistoryItem)?.let { currentItem ->
                    if (isFriendItem(currentItem)) {
                        when(position) {
                        // item not found
                            -1 -> {}
                        // first item
                            0 -> {
                                currentItem.isAvatarVisible.set(true)
                            }
                        // middle and last items
                            else -> {
                                (it[position - 1] as? HistoryItem)?.let { prevItem ->
                                    currentItem.isAvatarVisible.set(!isFriendItem(prevItem))
                                }
                            }
                        }
                    }
                }
            }
        }
        outRect?.setEmpty()
    }
}