package com.topface.topface.ui.fragments.feed.feed_base

import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId

/**
 * Адаптре для симпатий
 * Created by tiberal on 10.08.16.
 */
abstract class BaseSymphatiesFeedAdapter() : BaseFeedAdapter<FeedItemHeartBinding, FeedLike>() {

    override fun onViewRecycled(holder: ItemViewHolder?) {
        holder?.binding?.let {
            if (it is FeedItemHeartBinding) {
                it.model.release()
            }
        }
    }

    val handleDuplicates = { isOk: Boolean, userId: Int ->
        data.forEachIndexed { position, feedItem ->
            if (feedItem.getUserId() == userId) {
                feedItem.mutualed = isOk
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemLayout() = R.layout.feed_item_heart
}