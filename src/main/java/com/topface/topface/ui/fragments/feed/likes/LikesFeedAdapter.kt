package com.topface.topface.ui.fragments.feed.likes

import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Адаптре для симпатий
 * Created by tiberal on 10.08.16.
 */
class LikesFeedAdapter(private val mNavigator: IFeedNavigator, private val mApi: FeedApi) : BaseFeedAdapter<FeedLike>() {

    override fun bindData(binding: FeedItemHeartBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let {
            val item = data[position]
            it.likeItemViewModel = LikesItemViewModel(it, item, mNavigator, mApi, { isActionModeEnabled })
            binding.heart.isActivated = if (item.mutualed) true else false
        }
    }

    override fun onViewRecycled(holder: ItemViewHolder<*>?) {
        holder?.binding?.let {
            if (it is FeedItemHeartBinding) {
                it.likeItemViewModel.release()
            }
        }
    }

    override fun getItemLayout(): Int = R.layout.feed_item_heart

    override fun getItemBindingClass(): Class<FeedItemHeartBinding> = FeedItemHeartBinding::class.java

}