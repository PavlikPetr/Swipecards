package com.topface.topface.ui.fragments.feed.bookmarks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.topface.topface.data.FeedBookmark
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * Created by tiberal on 19.09.16.
 */
class BookmarksFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedBookmark>(binding, navigator, api) {

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
        /*


        mBookmarkedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.hasExtra(BlackListAndBookmarkHandler.TYPE) &&
                        intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE) == BlackListAndBookmarkHandler.ActionTypes.BOOKMARK && isAdded()) {
                    val ids = intent.getIntArrayExtra(BlackListAndBookmarkHandler.FEED_IDS)
                    val hasValue = intent.hasExtra(BlackListAndBookmarkHandler.VALUE)
                    val value = intent.getBooleanExtra(BlackListAndBookmarkHandler.VALUE, false)
                    if (hasValue) {
                        if (!value && ids != null) {
                           // getListAdapter().removeByUserIds(ids)
                            //TODO НУЖНО ДОБАВИТЬ ОБРАБОТКУ НОВОГО ИТЕМА
                        } else {
                          //  updateOnResume()
                        }
                    }
                }
            }
        }
        */
        LocalBroadcastManager.getInstance(context).registerReceiver(mBookmarkedReceiver,
                IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY))
    }

    override fun release() {
        super.release()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBookmarkedReceiver)
    }
}