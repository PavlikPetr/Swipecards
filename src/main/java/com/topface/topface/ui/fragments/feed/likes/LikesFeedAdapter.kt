package com.topface.topface.ui.fragments.feed.likes

import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseSymphatiesFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Адаптре для симпатий
 * Created by tiberal on 10.08.16.
 */
class LikesFeedAdapter(private val mNavigator: IFeedNavigator, private val mApi: FeedApi) : BaseSymphatiesFeedAdapter() {

    override fun bindData(binding: FeedItemHeartBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let { bind ->
            getDataItem(position)?.let {
                bind.model = LikesItemViewModel(bind, it, mNavigator, mApi, handleDuplicates) { isActionModeEnabled }
            }
        }
    }
}