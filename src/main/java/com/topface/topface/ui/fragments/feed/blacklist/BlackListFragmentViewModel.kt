package com.topface.topface.ui.fragments.feed.blacklist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.topface.topface.data.BlackListItem
import com.topface.topface.data.CountersData
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.fragments.feed.FeedFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.actionbar.OverflowMenu.USER_ID_FOR_REMOVE_FROM_BLACK_LIST
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

class BlackListFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<BlackListItem>(binding, navigator, api) {
    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        return false
    }

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_BLACK_LIST_FEEDS
    override val itemClass: Class<BlackListItem>
        get() = BlackListItem::class.java
    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.BLACK_LIST
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)

    private lateinit var mBlackListdReceiver: BroadcastReceiver

    override fun createAndRegisterBroadcasts() {
        super.createAndRegisterBroadcasts()
        mBlackListdReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null && intent.hasExtra(USER_ID_FOR_REMOVE_FROM_BLACK_LIST)) {
                    val item = mAdapter?.data?.find { blackListItem -> blackListItem.user.id == intent.getIntExtra(USER_ID_FOR_REMOVE_FROM_BLACK_LIST, 0) }
                    if (item != null) {
                        mAdapter?.removeItems(listOf(item))
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(mBlackListdReceiver,
                IntentFilter(FeedFragment.REFRESH_DIALOGS))
    }

    override fun release() {
        super.release()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBlackListdReceiver)
    }
}