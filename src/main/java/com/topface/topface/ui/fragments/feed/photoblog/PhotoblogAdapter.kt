package com.topface.topface.ui.fragments.feed.photoblog

import com.topface.topface.R
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.databinding.FeedPhotoblogItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator


class PhotoblogAdapter(private val mNavigator: IFeedNavigator) : BaseFeedAdapter<FeedPhotoblogItemBinding, FeedPhotoBlog>() {

    override fun bindData(binding: FeedPhotoblogItemBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let {
            val item = data[position]
            it.model = PhotoblogItemViewModel(it, item, mNavigator) { isActionModeEnabled }
        }
    }

    override fun getItemLayout() = R.layout.feed_photoblog_item

}