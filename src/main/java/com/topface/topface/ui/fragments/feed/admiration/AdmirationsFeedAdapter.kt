package com.topface.topface.ui.fragments.feed.likes

import com.flurry.sdk.it
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.feed.admiration.AdmirationItemViewModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId

/**
 * Адаптре для симпатий
 * Created by tiberal on 10.08.16.
 */
class AdmirationsFeedAdapter(private val mNavigator: IFeedNavigator, private val mApi: FeedApi) : LikesFeedAdapter(mNavigator, mApi) {

	override fun bindData(binding: FeedItemHeartBinding?, position: Int) {
		super.bindData(binding, position)
		binding?.let { bind ->
			getDataItem(position)?.let {
				bind.model = AdmirationItemViewModel(bind, it, mNavigator, mApi, handleDuplicates) { isActionModeEnabled }
			}
		}
	}
}