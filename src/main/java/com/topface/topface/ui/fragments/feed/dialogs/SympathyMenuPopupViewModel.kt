package com.topface.topface.ui.fragments.feed.dialogs

import com.topface.topface.R
import com.topface.topface.api.Api
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.utils.extensions.getString

class SympathyMenuPopupViewModel(private val mFeedBookmark: FeedBookmark, private val mApi: Api, private val iDialogCloser: IDialogCloser) : MenuPopupViewModel(mFeedBookmark.user) {

    override val deleteText: String
        get() = R.string.delete_mutual_sympathy.getString()

    override fun addToBlackListItem() {
        mApi.execAddToBlackList(listOf(mFeedBookmark.user.id))
        iDialogCloser.closeIt()
    }

    override fun deleteItem() {
        mApi.execDeleteMutual(arrayListOf(mFeedBookmark.user.id.toString()))
        iDialogCloser.closeIt()
    }

}