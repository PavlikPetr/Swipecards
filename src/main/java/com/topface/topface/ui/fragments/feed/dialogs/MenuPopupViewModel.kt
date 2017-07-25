package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.User
import com.topface.topface.glide.tranformation.GlideTransformationType
import com.topface.topface.utils.extensions.getDimen

/**
 * Базовый класс для попапменю
 */
abstract class MenuPopupViewModel(val user: FeedUser?) {

    val userPhoto = ObservableField(user?.photo)
    val type = ObservableField( GlideTransformationType.CROP_CIRCLE_TYPE)
    val placeholderRes = ObservableInt(if (user?.sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small)
    val onLineCircle = ObservableField(R.dimen.popup_menu_circle_online.getDimen())
    val strokeSize = ObservableField(R.dimen.popup_menu_stroke_outside.getDimen())

    init {
        user?.let{
            type.set(if (user.online) GlideTransformationType.ONLINE_CIRCLE_TYPE else GlideTransformationType.CROP_CIRCLE_TYPE)
        }
    }

    abstract val deleteText: String

    abstract fun addToBlackListItem()

    abstract fun deleteItem()

}