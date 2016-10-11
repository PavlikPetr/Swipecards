package com.topface.topface.ui.fragments.feed.admiration

import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.requests.ApiRequest
import com.topface.topface.requests.ReadAdmirationRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.ui.fragments.feed.likes.LikesItemViewModel

/**
 * Created by ppavlik on 11.10.16.
 * ViewModel for an admiration item
 */
class AdmirationItemViewModel(binding: FeedItemHeartBinding, item: FeedLike, navigator: IFeedNavigator,
                              mApi: FeedApi, mHandleDuplicates: (Boolean, Int) -> Unit,
                              isActionModeEnabled: () -> Boolean) :
        LikesItemViewModel(binding, item, navigator, mApi, mHandleDuplicates, isActionModeEnabled) {

    override fun getReadItemRequest() = ReadAdmirationRequest(context, listOf(item.id.toInt()))
}