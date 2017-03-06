package com.topface.topface.ui.auth

import android.content.Context
import android.databinding.ObservableInt
import com.topface.topface.utils.Utils.getStatusBarHeight

/**
 * VM для AuthFragment'a пока не полностью, но надо все переводить на нормальные технологии однажды
 */
class AuthFragmentViewModel(context: Context) {
    //todo remove this margin if experiment with DatingRedesign will be removed
    val topMargin = ObservableInt(getStatusBarHeight(context.applicationContext))
}