package com.topface.topface.ui.fragments.feed.visitors

import com.topface.topface.R
import com.topface.topface.data.Visitor
import com.topface.topface.databinding.FeedItemSimpleTimeBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.debug.FuckingVoodooMagic


class VisitorsAdapter(private val mNavigator: IFeedNavigator) : BaseFeedAdapter<FeedItemSimpleTimeBinding, Visitor>() {

    override fun getItemLayout() = R.layout.feed_item_simple_time

    override fun bindData(binding: FeedItemSimpleTimeBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let {
            binding.model = VisitorsItemViewModel(it, getDataItem(position), mNavigator) { isActionModeEnabled }
        }
    }

    @FuckingVoodooMagic(description = "костылина на сервере, id в формате 1:2 , 1 - время перехода, 2 - user id")
    override fun getItemId(position: Int) = getDataItem(position).id.hashCode().toLong()

}