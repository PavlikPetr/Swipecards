package com.topface.topface.ui.fragments.feed.dialogs

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.PopupMenuAddToBlackListEvent
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.PopupMenuDeleteEvent
import com.topface.topface.utils.extensions.getString

class SympathyMenuPopupViewModel(private val mFeedBookmark: FeedBookmark, private val iDialogCloser: IDialogCloser) : MenuPopupViewModel(mFeedBookmark.user) {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    override val deleteText: String
        get() = R.string.delete_mutual_sympathy.getString()

    override fun addToBlackListItem() {
        mEventBus.setData(PopupMenuAddToBlackListEvent(mFeedBookmark, PopupMenuFragment.MUTUAL_TYPE))
        iDialogCloser.closeIt()
    }

    override fun deleteItem() {
        mEventBus.setData(PopupMenuDeleteEvent(mFeedBookmark, PopupMenuFragment.MUTUAL_TYPE))
        iDialogCloser.closeIt()
    }

}