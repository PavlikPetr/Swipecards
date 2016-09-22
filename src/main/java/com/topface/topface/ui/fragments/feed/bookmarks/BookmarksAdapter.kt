package com.topface.topface.ui.fragments.feed.bookmarks

import com.topface.topface.R
import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.FeedBookmarksItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by tiberal on 19.09.16.
 */
class BookmarksAdapter(private val mNavigator: IFeedNavigator) : BaseFeedAdapter<FeedBookmarksItemBinding, FeedBookmark>() {

    override fun bindData(binding: FeedBookmarksItemBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let {
            binding.model = BookmarksItemViewModel(it, getDataItem(position), mNavigator) { isActionModeEnabled }
        }
    }

    override fun getItemLayout() = R.layout.feed_bookmarks_item
}