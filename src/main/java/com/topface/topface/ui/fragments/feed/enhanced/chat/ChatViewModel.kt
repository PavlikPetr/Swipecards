package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.ImprovedObservableList

class ChatViewModel : BaseViewModel() {

    val isListVisible = ObservableInt(View.VISIBLE)
    val data = ImprovedObservableList<Any>()

}
