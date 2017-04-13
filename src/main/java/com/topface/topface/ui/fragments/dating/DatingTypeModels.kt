package com.topface.topface.ui.fragments.dating

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import com.topface.topface.R
import com.topface.topface.data.Profile

/**
 * Created by mbulgakov on 12.04.17.
 */
data class FormModel(var data: Pair<String, String>? = null, var userId: Int? = null, var formType: Int = -1,
                     val isEmptyItem: Boolean, @DrawableRes var iconRes: Int,
                     @ColorRes var formItemBackground: Int = R.color.transparent, var onRequestSended: (() -> Unit)? = null)

data class ParentModel(val data: String, val isTitleItem: Boolean, val icon: Int)
data class GiftsModel(val gifts: Profile.Gifts?, val userId: Int)