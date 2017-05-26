package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import android.databinding.ObservableBoolean
import android.databinding.ObservableField

/**
 * Interface containing text and visibility for divider
 */
interface IDivider {
    val dividerText: ObservableField<String>
    val isDividerVisible: ObservableBoolean
}