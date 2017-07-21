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
import rx.Subscription

class AdmirationMenuPopupViewModel(private val mFeedBookmark: FeedBookmark, private val mApi: Api, private val iDialogCloser: IDialogCloser) : MenuPopupViewModel(mFeedBookmark.user) {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mAddToBlackListSubscriber: Subscription? = null
    private var mDeleteAdmirationSubscriber: Subscription? = null

    override val deleteText: String
        get() = R.string.delete_admiration.getString()

    override fun addToBlackListItem() {
        mAddToBlackListSubscriber = mApi.callAddToBlackList(listOf(mFeedBookmark)).subscribe({
            mEventBus.setData(PopupMenuAddToBlackListEvent(mFeedBookmark, PopupMenuFragment.ADMIRATION_TYPE))
        })
        iDialogCloser.closeIt()
    }

    override fun deleteItem() {
            mDeleteAdmirationSubscriber = mApi.callDeleteAdmiration(arrayListOf(mFeedBookmark.id.toString())).subscribe({
                mEventBus.setData(PopupMenuDeleteEvent(mFeedBookmark, PopupMenuFragment.ADMIRATION_TYPE))
            })
        iDialogCloser.closeIt()
    }

    override fun release() = arrayOf(mAddToBlackListSubscriber,mDeleteAdmirationSubscriber).safeUnsubscribe()

}