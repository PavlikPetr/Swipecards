package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.ChatObservableList

class ChatViewModel : BaseViewModel() {

    val isChatVisible = ObservableInt(View.VISIBLE)
    val chatData = ChatObservableList()

}
