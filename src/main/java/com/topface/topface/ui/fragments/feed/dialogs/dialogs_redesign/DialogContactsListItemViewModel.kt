package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.content.Intent
import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.User
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.state.EventBus
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.glide_utils.GlideTransformationType
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import javax.inject.Inject

/**
 * VM итема хедера в диалогах
 * Created by tiberal on 04.12.16.
 */
class DialogContactsListItemViewModel(private val mApi: FeedApi, private val mNavigator: IFeedNavigator
                                      , private var mItem: DialogContactsItem) : ILifeCycle {
    @Inject lateinit var mEventBus: EventBus
    val userPhoto = ObservableField(mItem.user.photo)
    val type = ObservableField(getTransformType())
    val placeholderRes = ObservableField(if (mItem.user.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small)
    val name = ObservableField(mItem.user.firstName)
    val nameTextColor = ObservableField(getNameColor())

    private var mItemUpdateEventSubscription: Subscription? = null

    init {
        App.get().inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (requestCode == ChatActivity.REQUEST_CHAT) {
                val userId = data.getIntExtra(ChatFragment.INTENT_USER_ID, -1)
                if (mItem.user.id == userId && !data.getBooleanExtra(ChatFragment.SEND_MESSAGE, false)) {
                    if (mItem.unread) {
                        sendReadRequest(mItem).first().subscribe {
                            if (it.completed) {
                                mItem.unread = false
                                nameTextColor.set(getNameColor())
                                mEventBus.setData(ContactsItemsReadEvent(mItem))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendReadRequest(dialogContacts: DialogContactsItem) = if (dialogContacts.highrate) {
        mApi.callAdmirationRead(listOf(dialogContacts.id))
    } else {
        mApi.callMutualRead(listOf(dialogContacts.id))
    }

    fun getNameColor() =
            if (mItem.unread) R.color.dialog_contacts_unread.getColor() else R.color.dialog_contacts_was_read.getColor()

    private fun getTransformType() = when {
        mItem.user.online && mItem.highrate -> GlideTransformationType.ADMIRATION_AND_ONLINE_TYPE
        !mItem.user.online && mItem.highrate -> GlideTransformationType.ADMIRATION_TYPE
        mItem.user.online -> GlideTransformationType.ADMIRATION_ONLINE_TYPE
        else -> GlideTransformationType.CROP_CIRCLE_TYPE
    }

    fun goChat() = mNavigator.showChat(mItem.user, null)

    fun release() = mItemUpdateEventSubscription.safeUnsubscribe()
}
