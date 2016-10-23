package com.topface.topface.ui.fragments.feed.likes

import android.databinding.ViewDataBinding
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.ui.fragments.feed.app_day.AppDayViewModel
import com.topface.topface.ui.fragments.feed.app_day.models.AppDayImage
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedAdapter
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId

/**
 * Адаптре для симпатий
 * Created by tiberal on 10.08.16.
 */
class LikesFeedAdapter(private val mNavigator: IFeedNavigator, private val mApi: FeedApi) : BaseFeedAdapter<FeedItemHeartBinding, FeedLike>() {

    override fun bindData(binding: FeedItemHeartBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let { bind ->
            getDataItem(position)?.let {
                bind.model = LikesItemViewModel(bind, it, mNavigator, mApi, handleDuplicates) { isActionModeEnabled }
            }
        }
    }

    override fun onViewRecycled(holder: BaseRecyclerViewAdapter.ItemViewHolder?) {
        holder?.binding?.let {
            if (it is FeedItemHeartBinding) {
                it.model.release()
            }
        }
    }

    override fun bindHeader(binding: ViewDataBinding?, position: Int) {
        binding?.let { bind ->
            (getHeaderItem(position) as? List<AppDayImage>)?.let {
                bind.setVariable(BR.viewModel, AppDayViewModel(bind as AppDayListBinding, it))
            }
        }
    }

    val handleDuplicates = { isOk: Boolean, userId: Int ->
        data.forEachIndexed { position, feedItem ->
            if (feedItem.getUserId().equals(userId)) {
                feedItem.mutualed = isOk
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemLayout() = R.layout.feed_item_heart


}