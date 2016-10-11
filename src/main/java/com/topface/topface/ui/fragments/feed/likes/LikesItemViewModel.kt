package com.topface.topface.ui.fragments.feed.likes

import android.content.Intent
import android.databinding.ViewDataBinding
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.data.Rate
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.requests.ApiRequest
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.BaseSymphatiesItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.ads.AdmobInterstitialUtils
import com.topface.topface.utils.cache.SearchCacheManager
import rx.Subscriber
import rx.Subscription

/**
 * VM для итема лайков
 * Created by tiberal on 15.08.16.
 */
class LikesItemViewModel(binding: FeedItemHeartBinding, item: FeedLike, navigator: IFeedNavigator,
                         mApi: FeedApi, mHandleDuplicates: (Boolean, Int) -> Unit,
                         isActionModeEnabled: () -> Boolean) :
        BaseSymphatiesItemViewModel<FeedItemHeartBinding>(binding, item, navigator, mApi,
                mHandleDuplicates, isActionModeEnabled) {

    override fun getClickListenerForMultiselectHandle() = arrayOf<View.OnClickListener>(binding.clickListener)

    override fun getReadItemRequest(): ApiRequest {
        return ReadLikeRequest(context, item.getUserId())
    }
}