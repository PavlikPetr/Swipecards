package com.topface.topface.utils.extensions

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.data.Profile
import com.topface.topface.data.User

/**
 * Profile related functions
 * Created by m.bayutin on 29.01.17.
 */
fun Profile.getPlaceholderRes() =
        if (sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small

fun FeedUser?.getPlaceholderRes() = if (this != null) {
    if (sex == User.BOY) R.drawable.dialogues_av_man_small else R.drawable.dialogues_av_girl_small
} else {
    if (App.get().profile.sex == User.BOY) R.drawable.dialogues_av_girl_small else R.drawable.dialogues_av_man_small
}


