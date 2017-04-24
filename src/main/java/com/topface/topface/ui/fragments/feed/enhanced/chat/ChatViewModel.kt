package com.topface.topface.ui.fragments.feed.enhanced.chat

import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseViewModel
import com.topface.topface.ui.fragments.feed.enhanced.utils.ChatData

class ChatViewModel : BaseViewModel() {

    val isChatVisible = ObservableInt(View.VISIBLE)
    val chatData = ChatData()

}
