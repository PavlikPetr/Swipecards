package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.ObservableField
import com.topface.topface.R
import com.topface.topface.data.User
import com.topface.topface.requests.response.DialogContactsItem
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.glide_utils.GlideTransformationType

/**
 * VM итема хедера в диалогах
 * Created by tiberal on 04.12.16.
 */
class DialogContactsListItemViewModel(private val mNavigator: IFeedNavigator
                                      , private val mItem: DialogContactsItem) {
    val userPhoto = ObservableField(mItem.user.photo)
    val type = ObservableField(getTransformType())
    val placeholderRes = ObservableField(if (mItem.user.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small)
    val name = ObservableField(mItem.user.firstName)

    private fun getTransformType() = when {
        mItem.user.online && mItem.highrate -> GlideTransformationType.ADMIRATION_AND_ONLINE_TYPE
        !mItem.user.online && mItem.highrate -> GlideTransformationType.ADMIRATION_TYPE
        mItem.user.online -> GlideTransformationType.DIALOG_ONLINE_TYPE
        else -> GlideTransformationType.CROP_CIRCLE_TYPE
    }

    fun goChat() = mNavigator.showChat(mItem.user, null)

}