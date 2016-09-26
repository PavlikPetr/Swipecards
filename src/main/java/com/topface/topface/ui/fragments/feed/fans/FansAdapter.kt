package com.topface.topface.ui.fragments.feed.fans

import android.support.v7.widget.RecyclerView
import com.topface.topface.R
import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.FeedItemSimpleLayoutBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.debug.FuckingVoodooMagic

/**
 * Created by tiberal on 09.09.16.
 */
class FansAdapter(private val mNavigator: IFeedNavigator) : BaseFeedAdapter<FeedItemSimpleLayoutBinding, FeedBookmark>() {

    override fun getItemLayout() = R.layout.feed_item_simple_layout

    override fun bindData(binding: FeedItemSimpleLayoutBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let { bind ->
            getDataItem(position)?.let {
                binding.model = FansItemViewModel(bind, it, mNavigator) { isActionModeEnabled }
            }
        }
    }

    @FuckingVoodooMagic(description = "костылина на сервере, id в формате 1:2 , 1 - время перехода, 2 - user id")
    override fun getItemId(position: Int) = getDataItem(position)?.id?.hashCode()?.toLong() ?: RecyclerView.NO_ID

}