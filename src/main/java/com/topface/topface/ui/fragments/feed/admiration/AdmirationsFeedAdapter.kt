package com.topface.topface.ui.fragments.feed.admiration

import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseSymphatiesFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by ppavlik on 11.10.16.
 * Admirations adapter
 */
class AdmirationsFeedAdapter(private val mNavigator: IFeedNavigator, private val mApi: FeedApi) : BaseSymphatiesFeedAdapter() {

    override fun bindData(binding: FeedItemHeartBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let { bind ->
            getDataItem(position)?.let {
                bind.model = AdmirationItemViewModel(bind, it, mNavigator, mApi, handleDuplicates) { isActionModeEnabled }
            }
        }
    }
}