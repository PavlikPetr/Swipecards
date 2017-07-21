package com.topface.topface.ui.fragments.feed.dialogs

import com.topface.topface.R
import com.topface.topface.api.Api
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.utils.extensions.getString

class AdmirationMenuPopupViewModel(private val mFeedBookmark: FeedBookmark, private val mApi: Api, private val iDialogCloser: IDialogCloser) : MenuPopupViewModel(mFeedBookmark.user) {

    override val deleteText: String
        get() = R.string.delete_admiration.getString()

    override fun addToBlackListItem() {
        mFeedBookmark.user?.let {
            mApi.execAddToBlackList(listOf(it.id))
        }
        iDialogCloser.closeIt()
    }

    override fun deleteItem() {
        mFeedBookmark.user?.let {
            mApi.execDeleteAdmiration(arrayListOf(it.id.toString()))
        }
        iDialogCloser.closeIt()
    }

}