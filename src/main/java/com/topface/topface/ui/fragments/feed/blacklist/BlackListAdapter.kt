package com.topface.topface.ui.fragments.feed.blacklist

import com.flurry.sdk.it
import com.topface.topface.R
import com.topface.topface.data.BlackListItem
import com.topface.topface.databinding.BlackListItemBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

class BlackListAdapter(private val mNavigator: IFeedNavigator) : BaseFeedAdapter<BlackListItemBinding, BlackListItem>() {

    override fun bindData(binding: BlackListItemBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let { bind ->
            getDataItem(position)?.let {
                binding.model = BlackListItemViewModel(bind, it, mNavigator) { isActionModeEnabled }
            }
        }
    }

    override fun getItemLayout() = R.layout.black_list_item
}