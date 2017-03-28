package com.topface.topface.chat.vm

import android.databinding.ObservableInt
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.chat.IComplainHeaderActionListener

/**
 * VM for chat screen (partial)
 * Created by m.bayutin on 27.03.17.
 */
class ChatViewModel {
    val complainActionListener = object : IComplainHeaderActionListener {
        override fun onComplain() {
            Debug.error("ChatViewModel:: onComplain not implemented")
        }

        override fun onBlock() {
            Debug.error("ChatViewModel:: onBlock not implemented")
        }

        override fun onClose() {
            complainVisibility.set(View.GONE)
        }
    }

    val complainVisibility = ObservableInt(View.VISIBLE)
}