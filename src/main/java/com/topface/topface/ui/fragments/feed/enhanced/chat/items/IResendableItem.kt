package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableBoolean

/**
 * for items which could be sent/resent
 */
interface IResendableItem {
    val isErrorVisible: ObservableBoolean
    val isSending: ObservableBoolean
    val isRetrierVisible: ObservableBoolean
    fun onRetryClick() {}
}