package com.topface.topface.ui.fragments.feed.mutual

import com.topface.topface.R
import com.topface.topface.data.FeedMutual
import com.topface.topface.databinding.FeedItemCityAgeNameBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator


class MutualFeedAdapter(private val mNavigator: IFeedNavigator) : BaseFeedAdapter<FeedItemCityAgeNameBinding, FeedMutual>() {

    override fun bindData(binding: FeedItemCityAgeNameBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let {
            val item = getDataItem(position)
            it.model = BaseFeedItemViewModel(it, item, mNavigator, { isActionModeEnabled })
            it.city = item.user.city
        }
    }

    override fun getItemLayout() = R.layout.feed_item_city_age_name
}