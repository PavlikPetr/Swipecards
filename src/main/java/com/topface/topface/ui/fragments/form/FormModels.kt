package com.topface.topface.ui.fragments.form

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import com.topface.topface.R
import com.topface.topface.data.Profile

/**
 *  Модельки для анкеты
 * Created by tiberal on 02.11.16.
 */

data class FormModel(var data: Pair<String, String>? = null, var userId: Int? = null, var formType: Int = -1,
                     val isEmptyItem: Boolean, @DrawableRes var iconRes: Int = R.drawable.bt_question,
                     @ColorRes var formItemBackground: Int = R.color.transparent) : IType {
    override fun getType() = 0
}

data class ParentModel(val data: String, val isTitleItem: Boolean, val icon: Int) : IType {
    override fun getType() = 1
}

data class GiftsModel(val gifts: Profile.Gifts?, val userId: Int) : IType {
    override fun getType() = 2
}