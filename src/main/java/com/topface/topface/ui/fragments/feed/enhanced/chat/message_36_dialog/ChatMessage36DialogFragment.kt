package com.topface.topface.ui.fragments.feed.enhanced.chat.message_36_dialog

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.DialogChatMessage36Binding
import com.topface.topface.ui.PurchasesActivity
import com.topface.topface.ui.analytics.TrackedDialogFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import org.jetbrains.anko.layoutInflater

/**
 * HOW TO USE from ChatFragment (java)
 * (ChatMessage36DialogFragment.Companion.newInstance(mUser)).show(getActivity().getSupportFragmentManager(), ChatMessage36DialogFragment.TAG);
 */
class ChatMessage36DialogFragment : TrackedDialogFragment() {
    companion object {
        const val TAG = "ChatMessage36DialogFragment.Tag"
        const val TAG_FOR_PURCHASE = "PopularUserBlockDialog"
        internal const val ARG_USER = "ChatMessage36DialogFragment.ArgUser"

        fun newInstance(user: FeedUser) = ChatMessage36DialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_USER, user)
            }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<DialogChatMessage36Binding>(context.layoutInflater, R.layout.dialog_chat_message_36, null, false)
    }

    private val mFeedNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.RoundedPopup)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View = with(mBinding) {
        viewModel = ChatMessage36DialogViewModel(arguments) {
            mFeedNavigator.showPurchaseVip(TAG_FOR_PURCHASE)
            dialog.dismiss()
        }
        super.onCreateView(inflater, container, savedInstanceState)
        root
    }
}