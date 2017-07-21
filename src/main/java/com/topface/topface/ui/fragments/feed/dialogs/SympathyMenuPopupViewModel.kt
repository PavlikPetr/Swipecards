package com.topface.topface.ui.fragments.feed.dialogs

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.Api
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.PopupMenuAddToBlackListEvent
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.PopupMenuDeleteEvent
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscribe
import rx.Subscription

class SympathyMenuPopupViewModel(private val mFeedBookmark: FeedBookmark, private val mApi: Api, private val iDialogCloser: IDialogCloser) : MenuPopupViewModel(mFeedBookmark.user) {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mAddToBlackListSubscriber: Subscription? = null
    private var mDeleteMutualSubscriber: Subscription? = null

    override val deleteText: String
        get() = R.string.delete_mutual_sympathy.getString()

    override fun addToBlackListItem() {
        mAddToBlackListSubscriber = mApi.callAddToBlackList(listOf(mFeedBookmark)).shortSubscribe({
            mEventBus.setData(PopupMenuAddToBlackListEvent(mFeedBookmark, PopupMenuFragment.MUTUAL_TYPE))
        })
        iDialogCloser.closeIt()
    }

    override fun deleteItem() {
        mFeedBookmark.user?.let {
            mDeleteMutualSubscriber = mApi.callDeleteMutual(arrayListOf(it.id.toString())).subscribe({
                mEventBus.setData(PopupMenuDeleteEvent(mFeedBookmark, PopupMenuFragment.MUTUAL_TYPE))
            })
        }
        iDialogCloser.closeIt()
    }

    override fun release() = arrayOf(mAddToBlackListSubscriber, mDeleteMutualSubscriber).safeUnsubscribe()
}