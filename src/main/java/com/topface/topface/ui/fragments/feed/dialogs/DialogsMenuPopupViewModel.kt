package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.User
import com.topface.topface.state.EventBus
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.DialogPopupEvent
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.glide_utils.GlideTransformationType
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscriber
import rx.Subscription
import javax.inject.Inject


class DialogsMenuPopupViewModel(private val mFeedDialog: FeedDialog, private val mApi: FeedApi, private val iDialogCloser: IDialogCloser) {

    @Inject lateinit var mEventBus: EventBus

    init {
        App.get().inject(this)
    }

    private var mBlackListSubscriber: Subscription? = null
    private var mDeleteDialogsSubscriber: Subscription? = null
    val userPhoto = ObservableField(mFeedDialog.user.photo)
    val type = ObservableField(if (mFeedDialog.user.online) GlideTransformationType.DIALOG_ONLINE_TYPE else GlideTransformationType.CROP_CIRCLE_TYPE)
    val placeholderRes = ObservableField(if (mFeedDialog.user.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small)

    fun deleteDialog() {
        mDeleteDialogsSubscriber = mApi.callDelete(FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS, ids = arrayListOf(mFeedDialog.user.id.toString())).subscribe(object : Subscriber<Boolean>() {
            override fun onError(e: Throwable?) = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onCompleted() = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onNext(t: Boolean?) {
                mEventBus.setData(DialogPopupEvent(mFeedDialog))
            }

        })
        iDialogCloser.closeIt()
    }

    fun addToBlackList() {
        mBlackListSubscriber = mApi.callAddToBlackList(items = listOf(mFeedDialog)).subscribe(object : Subscriber<Boolean>() {
            override fun onError(e: Throwable?) = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onCompleted() = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onNext(t: Boolean?) {
                mEventBus.setData(DialogPopupEvent(mFeedDialog))
            }
        })
        iDialogCloser.closeIt()

    }

}