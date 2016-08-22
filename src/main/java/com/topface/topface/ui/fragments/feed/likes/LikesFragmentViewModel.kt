package com.topface.topface.ui.fragments.feed.likes

import android.view.View
import com.topface.topface.App
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.ads.AdmobInterstitialUtils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * VM для фрагмента лайков
 * Created by tiberal on 08.08.16.
 */
class LikesFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, mApi: FeedApi) :
        BaseFeedFragmentViewModel<FeedLike>(binding, navigator, mApi) {

    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_LIKE)

    override val isNeedReadItems: Boolean
        get() = true

    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.LIKES

    override val itemClass: Class<FeedLike>
        get() = FeedLike::class.java

    override val isForPremium: Boolean
        get() = true

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS

    override fun itemClick(view: View?, itemPosition: Int, data: FeedLike?) {
        super.itemClick(view, itemPosition, data)
        ReadLikeRequest(context, data.getUserId()).exec()
        AdmobInterstitialUtils.
                requestPreloadedInterstitial(context, App.get().options.interstitial)
    }
}