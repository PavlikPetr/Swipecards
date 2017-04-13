package com.topface.topface.ui.fragments.feed.mutual

import android.view.View
import com.topface.topface.data.FeedLike
import com.topface.topface.data.FeedMutual
import com.topface.topface.databinding.FeedItemCityAgeNameBinding
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.BaseSymphatiesItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId

/**
 * VM для итема лайков
 * Created by tiberal on 15.08.16.
 */
class MutualItemViewModel(binding: FeedItemCityAgeNameBinding, item: FeedMutual, navigator: IFeedNavigator,
                          isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedItemCityAgeNameBinding, FeedMutual>(binding, item, navigator, isActionModeEnabled) {

    fun getCity() = item.user.city.name

    override val feed_type: String
        get() = "Mutual"

    override fun getClickListenerForMultiselectHandle() = arrayOf<View.OnClickListener>(binding.clickListener)
}