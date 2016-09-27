package com.topface.topface.ui.fragments.feed.dialogs

import android.content.Intent
import com.topface.framework.utils.Debug
import com.topface.topface.data.CountersData
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.History
import com.topface.topface.databinding.FragmentFeedBaseBinding
import com.topface.topface.requests.FeedRequest
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragmentViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.DateUtils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.gcmutils.GCMUtils

class DialogsFragmentViewModel(binding: FragmentFeedBaseBinding, navigator: IFeedNavigator, api: FeedApi) :
        BaseFeedFragmentViewModel<FeedDialog>(binding, navigator, api) {
    override fun isCountersChanged(newCounters: CountersData, currentCounters: CountersData): Boolean {
        return newCounters.dialogs != currentCounters.dialogs
    }

    override val feedsType: FeedsCache.FEEDS_TYPE
        get() = FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS
    override val itemClass: Class<FeedDialog>
        get() = FeedDialog::class.java
    override val service: FeedRequest.FeedService
        get() = FeedRequest.FeedService.DIALOGS
    override val gcmType: Array<Int>
        get() = arrayOf(GCMUtils.GCM_TYPE_DIALOGS, GCMUtils.GCM_TYPE_MESSAGE, GCMUtils.GCM_TYPE_GIFT)
    override val gcmTypeUpdateAction: String?
        get() = GCMUtils.GCM_DIALOGS_UPDATE

    fun updatePreview(data: Intent) {
        val history = data.getParcelableExtra<History>(ChatActivity.LAST_MESSAGE)
        val userId = data.getIntExtra(ChatActivity.LAST_MESSAGE_USER_ID, -1)
        if (history != null && userId > 0) {
            val adapter = mAdapter
            if (adapter is DialogsAdapter) {
                for (i in 0..adapter.itemCount - 1) {
                    val dialog = adapter.getDataItem(i)
                    if (dialog != null && dialog.user != null && dialog.user.id == userId) {
                        dialog.type = history.type
                        dialog.text = history.text
                        dialog.target = history.target
                        dialog.createdRelative = DateUtils.getRelativeDate(dialog.created, true)
                        adapter.notifyItemChange(i)
                    }
                }
            }
        }
    }

    override fun considerDuplicates(first: FeedDialog, second: FeedDialog) = first.user?.id == second.user?.id
}

