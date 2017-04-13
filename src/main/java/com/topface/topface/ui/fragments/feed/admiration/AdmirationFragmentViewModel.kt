package com.topface.topface.ui.fragments.feed.admiration

import android.view.View
import com.topface.topface.App
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedLike
import com.topface.topface.data.Visitor
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.ReadAdmirationRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.ads.AdmobInterstitialUtils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * VM для фрагментма восхищений
 * Created by siberia87 on 30.09.16.
 */
class AdmirationFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedLike>(binding, navigator, api) {

    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData) =
            newCounters.admirations > currentCounters.admirations

    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_ADMIRATION)

    override val gcmTypeUpdateAction: String?
        get() = GCMUtils.GCM_ADMIRATION_UPDATE

    override val isNeedReadItems: Boolean
        get() = true

    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.ADMIRATIONS

    override val itemClass: Class<FeedLike>
        get() = FeedLike::class.java

    override val isForPremium: Boolean
        get() = true

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_ADMIRATION_FEEDS

    override fun itemClick(view: View?, itemPosition: Int, data: FeedLike?) {
        super.itemClick(view, itemPosition, data)
        data?.id?.toInt()?.let {
            ReadAdmirationRequest(context, listOf(it)).exec()
        }
        AdmobInterstitialUtils.
                requestPreloadedInterstitial(context, App.get().options.interstitial)
    }
}