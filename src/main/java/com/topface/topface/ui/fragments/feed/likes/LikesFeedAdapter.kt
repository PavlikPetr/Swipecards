package com.topface.topface.ui.fragments.feed.likes

import android.databinding.ViewDataBinding
import com.topface.billing.InstantPurchaseModel
import com.topface.topface.BR
import com.topface.topface.R
import com.topface.topface.databinding.AppDayListBinding
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.ui.fragments.feed.app_day.AppDayImage
import com.topface.topface.ui.fragments.feed.app_day.AppDayViewModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseSymphatiesFeedAdapter

/**
 * Адаптре для симпатий
 * Created by tiberal on 10.08.16.
 */
class LikesFeedAdapter(private val mInstantPurchaseModel: InstantPurchaseModel, private val mApi: FeedApi) : BaseSymphatiesFeedAdapter() {

    override fun bindData(binding: FeedItemHeartBinding?, position: Int) {
        super.bindData(binding, position)
        binding?.let { bind ->
            getDataItem(position)?.let {
                bind.model = LikesItemViewModel(bind, it, mInstantPurchaseModel.navigator, mApi, handleDuplicates) { isActionModeEnabled }
            }
        }
    }

    override fun bindHeader(binding: ViewDataBinding?, position: Int) {
        binding?.let { bind ->
            (getHeaderItem(position) as? List<AppDayImage>)?.let {
                bind.setVariable(BR.viewModel, AppDayViewModel(bind as AppDayListBinding, it, mInstantPurchaseModel))
                bind.setVariable(BR.plc,"likes_fragment_app_day_loader")
            }
        }
    }

    override fun getItemLayout() = R.layout.feed_item_heart
}