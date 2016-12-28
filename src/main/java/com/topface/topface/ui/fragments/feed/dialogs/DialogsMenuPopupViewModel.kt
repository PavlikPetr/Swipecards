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

/**
 * Created by mbulgakov on 05.12.16.
 */
class DialogsMenuPopupViewModel(private val item: FeedDialog,
                                private val mApi: FeedApi,
                                private val iDialogCloser: IDialogCloser) {

    @Inject lateinit var mEventBus: EventBus

    init {
        App.get().inject(this)
    }

    private var mBlackListSubscriber: Subscription? = null
    private var mDeleteDialogsSubscriber: Subscription? = null
    val userPhoto = ObservableField(item.user.photo)
    val type = ObservableField(if (item.user.online) GlideTransformationType.DIALOG_ONLINE_TYPE else GlideTransformationType.CROP_CIRCLE_TYPE)
    val placeholderRes = ObservableField(if (item.user.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small)

    fun deleteDialog() {
        mDeleteDialogsSubscriber = mApi.callDelete(FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS, ids = arrayListOf(item.user.id.toString())).subscribe(object : Subscriber<Boolean>() {
            override fun onError(e: Throwable?) = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onCompleted() = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onNext(t: Boolean?) {
                mEventBus.setData(DialogPopupEvent(item))
                mDeleteDialogsSubscriber.safeUnsubscribe()
            }

        })
        iDialogCloser.closeIt()
    }

    fun addToBlackList() {
        mBlackListSubscriber = mApi.callAddToBlackList(items = listOf(item)).subscribe(object : Subscriber<Boolean>() {
            override fun onError(e: Throwable?) = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onCompleted() = mDeleteDialogsSubscriber.safeUnsubscribe()

            override fun onNext(t: Boolean?) {
                mEventBus.setData(DialogPopupEvent(item))
                mDeleteDialogsSubscriber.safeUnsubscribe()
            }
        })
        iDialogCloser.closeIt()

    }
}