package com.topface.topface.ui.fragments.dating.form

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.ui.new_adapter.IType

/**
 *  Модельки для анкеты
 * Created by tiberal on 02.11.16.
 */

data class FormModel(var data: Pair<String, String>? = null, var userId: Int? = null, var formType: Int = -1,
                     val isEmptyItem: Boolean, @DrawableRes var iconRes: Int,
                     @ColorRes var formItemBackground: Int = R.color.transparent,var onRequestSended:(()->Unit)? = null) : IType {
    override fun getType() = 0
}

data class ParentModel(val data: String, val isTitleItem: Boolean, val icon: Int) : IType {
    override fun getType() = 1
}

data class GiftsModel(val gifts: Profile.Gifts?, val userId: Int) : IType {
    override fun getType() = 2
}