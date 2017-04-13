package com.topface.topface.ui.fragments.feed.admiration

import android.view.View
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.requests.ReadAdmirationRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseSymphatiesItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator

/**
 * Created by ppavlik on 11.10.16.
 * ViewModel for an admiration item
 */
class AdmirationItemViewModel(binding: FeedItemHeartBinding, item: FeedLike, navigator: IFeedNavigator,
                              mApi: FeedApi, mHandleDuplicates: (Boolean, Int) -> Unit,
                              isActionModeEnabled: () -> Boolean) :
        BaseSymphatiesItemViewModel<FeedItemHeartBinding>(binding, item, navigator, mApi,
                mHandleDuplicates, isActionModeEnabled) {

    override val feed_type: String
        get() = "Admiration"

    override fun getClickListenerForMultiselectHandle() = arrayOf<View.OnClickListener>(binding.clickListener)

    override fun getReadItemRequest() = ReadAdmirationRequest(context, listOf(item.id.toInt()))
}