package com.topface.topface.ui.fragments.feed.photoblog

import android.view.View
import com.topface.topface.App
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * VM for photoblog
 * Created by tiberal on 05.09.16.
 */
class PhotoblogFragmentViewModel(binding: FragmentFeedBaseBinding, private val mNavigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedPhotoBlog>(binding, mNavigator, api) {
    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        return false
    }

    private var mRefreshIntervalSubscription: Subscription

    companion object {
        private const val PHOTOBLOG_TAG = "photoblog_fragment"
        private val UPDATE_DELAY = 20L
    }

    init {
        mRefreshIntervalSubscription = Observable.interval(UPDATE_DELAY, UPDATE_DELAY, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(shortSubscription {
            loadTopFeeds()
        })
    }

    override val typeFeedFragment: String?
        get() = null

    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)

    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.PHOTOBLOG

    override val itemClass: Class<FeedPhotoBlog>
        get() = FeedPhotoBlog::class.java

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.UNKNOWN_TYPE

    override val isNeedCacheItems: Boolean
        get() = false

    override fun itemClick(view: View?, itemPosition: Int, data: FeedPhotoBlog?, from: String) =
            if (App.get().profile.uid == data?.user?.id) {
                mNavigator.showOwnProfile()
            } else {
                super.itemClick(view, itemPosition, data, PHOTOBLOG_TAG)
            }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mRefreshIntervalSubscription)
    }

}