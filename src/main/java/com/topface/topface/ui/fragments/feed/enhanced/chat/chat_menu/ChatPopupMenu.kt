package com.topface.topface.ui.fragments.feed.enhanced.chat.chat_menu

import android.content.ClipboardManager
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.databinding.ChatPopupMenuBinding
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate

class ChatPopupMenu : DialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "ChatPopupMenu"
        const val CHAT_ITEM = "chat_item"
        const val CHAT_ITEM_ID = "chat_item_position"

        fun newInstance(item: HistoryItem, itemId: Int) = ChatPopupMenu().apply {
            arguments = Bundle().apply {
                putParcelable(CHAT_ITEM, item)
                putInt(CHAT_ITEM_ID, itemId)
            }
        }
    }

    private val mClipboardManager by lazy {
        activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val mApi by lazy { App.getAppComponent().api() }

    private val mBinding by lazy {
        DataBindingUtil.inflate<ChatPopupMenuBinding>(activity.layoutInflater, R.layout.chat_popup_menu, null, false)
    }

    private val mViewModel by lazy {
        ChatPopupMenuViewModel(arguments, this, mClipboardManager, mApi).apply {
            activity.registerLifeCycleDelegate(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = with(mBinding) {
        viewModel = mViewModel
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        root
    }

    override fun onDetach() {
        super.onDetach()
        activity.unregisterLifeCycleDelegate(mViewModel)
        mViewModel.release()
    }

    override fun closeIt() = dialog.cancel()
}