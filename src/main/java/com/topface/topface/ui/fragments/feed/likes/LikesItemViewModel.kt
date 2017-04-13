package com.topface.topface.ui.fragments.feed.likes

import android.view.View
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseSymphatiesItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId

/**
 * VM для итема лайков
 * Created by tiberal on 15.08.16.
 */
class LikesItemViewModel(binding: FeedItemHeartBinding, item: FeedLike, navigator: IFeedNavigator,
                         mApi: FeedApi, mHandleDuplicates: (Boolean, Int) -> Unit,
                         isActionModeEnabled: () -> Boolean) :
        BaseSymphatiesItemViewModel<FeedItemHeartBinding>(binding, item, navigator, mApi,
                mHandleDuplicates, isActionModeEnabled) {

    override val feed_type: String
        get() = "Likes"

    override fun getClickListenerForMultiselectHandle() = arrayOf<View.OnClickListener>(binding.clickListener)

    override fun getReadItemRequest() = ReadLikeRequest(context, item.getUserId())
}