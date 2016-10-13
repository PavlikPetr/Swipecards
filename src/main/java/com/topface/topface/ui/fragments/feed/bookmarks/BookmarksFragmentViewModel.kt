package com.topface.topface.ui.fragments.feed.bookmarks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

class BookmarksFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedBookmark>(binding, navigator, api) {
    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        return false
    }

    override val typeFeedFragment: String?
        get() = null
    var mIsNeedToUpdate: Boolean = false
    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_BOOKMARKS_FEEDS
    override val itemClass: Class<FeedBookmark>
        get() = FeedBookmark::class.java
    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.BOOKMARKS
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_UNKNOWN)

    private lateinit var mBookmarkedReceiver: BroadcastReceiver

    override fun createAndRegisterBroadcasts() {
        super.createAndRegisterBroadcasts()
        mBookmarkedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null && intent.hasExtra(BlackListAndBookmarkHandler.TYPE) &&
                        intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE) == BlackListAndBookmarkHandler.ActionTypes.BOOKMARK) {
                    val ids = intent.getIntArrayExtra(BlackListAndBookmarkHandler.FEED_IDS)
                    val hasValue = intent.hasExtra(BlackListAndBookmarkHandler.VALUE)
                    val value = intent.getBooleanExtra(BlackListAndBookmarkHandler.VALUE, false)
                    if (hasValue) {
                        if (!value && ids != null) {
                            var deletedUsers = listOf<FeedBookmark>()
                            ids.forEach { id ->
                                mAdapter?.data?.forEach forEachData@{
                                    if (it.user.id == id) {
                                        deletedUsers = deletedUsers.plus(it)
                                        return@forEachData
                                    }
                                }
                            }
                            mAdapter?.let { adapter ->
                                adapter.removeItems(deletedUsers)
                                if (adapter.data?.isEmpty() ?: false) {
                                    isListVisible.set(View.INVISIBLE)
                                    stubView?.onEmptyFeed()
                                }
                            }

                        } else {
                            mIsNeedToUpdate = true
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(mBookmarkedReceiver,
                IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY))
    }

    override fun release() {
        super.release()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBookmarkedReceiver)
    }

    fun onResume() {
        if (mIsNeedToUpdate) {
            loadTopFeeds()
            mIsNeedToUpdate = false
        }
    }
}