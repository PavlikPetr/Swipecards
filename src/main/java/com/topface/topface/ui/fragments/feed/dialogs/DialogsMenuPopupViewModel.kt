package com.topface.topface.ui.fragments.feed.dialogs

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogPopupEvent
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscriber
import rx.Subscription

class DialogsMenuPopupViewModel(private val mFeedDialog: FeedDialog, private val mApi: FeedApi, private val iDialogCloser: IDialogCloser) : MenuPopupViewModel(mFeedDialog.user) {

    override val deleteText: String
    get() = R.string.popup_delete_dialog.getString()

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mBlackListSubscriber: Subscription? = null
    private var mDeleteDialogsSubscriber: Subscription? = null

    override fun addToBlackListItem() {
        mBlackListSubscriber = mApi.callAddToBlackList(items = listOf(mFeedDialog)).subscribe(object : Subscriber<Boolean>() {
            override fun onError(e: Throwable?) = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onCompleted() = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onNext(t: Boolean?) {
                mEventBus.setData(DialogPopupEvent(mFeedDialog))
            }
        })
        iDialogCloser.closeIt()
    }

    override fun deleteItem() {
        mDeleteDialogsSubscriber = mApi.callDelete(FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS, ids = arrayListOf(mFeedDialog.user.id.toString())).subscribe(object : Subscriber<Boolean>() {
            override fun onError(e: Throwable?) = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onCompleted() = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onNext(t: Boolean?) {
                mEventBus.setData(DialogPopupEvent(mFeedDialog))
            }

        })
        iDialogCloser.closeIt()
    }

}